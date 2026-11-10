package co.xendit.components.util

import co.xendit.components.data.encryption.XenditEncryption
import co.xendit.components.data.model.ChannelFormField
import co.xendit.components.data.model.Country
import co.xendit.components.data.model.primaryChannelPropertyKey

import co.xendit.components.data.model.FieldType
import co.xendit.components.data.model.InstallmentPlan

internal object PaymentRequestMapper {

  /**
   * Maps flat form values (dot-notated keys) to a nested map structure as required by the payment
   * API. Handles encryption for sensitive fields based on their type.
   */
  fun mapFormValuesToChannelProperties(
    formValues: Map<String, String>,
    fields: List<ChannelFormField>,
    publicKey: String,
    sessionId: String,
    installmentPlans: List<InstallmentPlan>? = null
  ): Map<String, Any> {
    val flatMap = mutableMapOf<String, Any>()

    fields.forEach { field ->
      val propertyKey = field.primaryChannelPropertyKey()
      val value = formValues[propertyKey] ?: return@forEach

      val isSensitive = isSensitiveField(field)

      when (val prop = field.channelProperty) {
        is String -> {
          var finalValue = if (isSensitive) encrypt(value, publicKey, sessionId) else value
          if (field.type is FieldType.PhoneNumber) {
            val countryCodeKey = "${propertyKey}_country_code"
            val countryCode = formValues[countryCodeKey] ?: ""
            finalValue = formatPhoneNumber(value, countryCode)
          }
          putFlatValue(flatMap, prop, finalValue)
        }

        is List<*> -> {
          if (field.type is FieldType.CreditCardExpiry && prop.size >= 2) {
            val monthKey = prop[0].toString()
            val yearKey = prop[1].toString()
            val (month, year) = splitExpiry(value)

            flatMap[monthKey] = if (isSensitive) encrypt(month, publicKey, sessionId) else month
            flatMap[yearKey] = if (isSensitive) encrypt(year, publicKey, sessionId) else year
          } else if (field.type is FieldType.InstallmentPlan && prop.size >= 2) {
            val termsKey = prop[0].toString()
            val intervalKey = prop[1].toString()
            val termsNum = value.toIntOrNull()
            
            if (termsNum != null && termsNum > 0) {
              val plan = installmentPlans?.find { it.terms == termsNum }
              if (plan != null) {
                flatMap[termsKey] = termsNum
                if (plan.interval != null) flatMap[intervalKey] = plan.interval
              }
            }
          } else {
            val key = prop.firstOrNull()?.toString() ?: ""
            if (key.isNotEmpty()) {
              var finalValue = if (isSensitive) encrypt(value, publicKey, sessionId) else value
              if (field.type is FieldType.PhoneNumber) {
                val countryCodeKey = "${propertyKey}_country_code"
                val countryCode = formValues[countryCodeKey] ?: ""
                finalValue = formatPhoneNumber(value, countryCode)
              }
              putFlatValue(flatMap, key, finalValue)
            }
          }
        }
      }
    }

    return unflatten(flatMap)
  }

  private fun formatPhoneNumber(value: String, countryCode: String): String {
    if (value.startsWith("+")) return value
    val dialCode = Country.fromCode(countryCode)?.dialCode.orEmpty()
    return if (dialCode.isNotEmpty() && value.startsWith(dialCode)) {
      "+$value"
    } else {
      "+$dialCode$value"
    }
  }

  private fun putFlatValue(flatMap: MutableMap<String, Any>, key: String, value: Any) {
    if (!key.endsWith("[]")) {
      flatMap[key] = value
      return
    }

    val normalizedKey = key.removeSuffix("[]")

    val newList = when (val existing = flatMap[normalizedKey]) {
      null -> mutableListOf(value)
      is Collection<*> -> existing.toMutableList().apply {
        if (!contains(value)) add(value)
      }
      else -> if (existing == value) mutableListOf(existing) else mutableListOf(existing, value)
    }

    flatMap[normalizedKey] = newList
  }

  private fun isSensitiveField(field: ChannelFormField): Boolean {
    return field.type is FieldType.CreditCardNumber || 
           field.type is FieldType.CreditCardExpiry || 
           field.type is FieldType.CreditCardCvn
  }

  private fun encrypt(value: String, publicKey: String, sessionId: String): String {
    //    val cleaned = value.replace("\\s".toRegex(), "")
    val cleaned = value
    return if (cleaned.isBlank()) "" else XenditEncryption.encrypt(cleaned, publicKey, sessionId)
  }

  private fun splitExpiry(expiry: String): Pair<String, String> {
    return if (expiry.length == 4) {
      expiry.take(2) to "20${expiry.takeLast(2)}"
    } else {
      "" to ""
    }
  }

  private fun unflatten(flatMap: Map<String, Any>): Map<String, Any> {
    val result = mutableMapOf<String, Any>()
    for ((key, value) in flatMap) {
      val parts = key.split(".")
      var current = result
      for (i in 0 until parts.size - 1) {
        val part = parts[i]

        @Suppress("UNCHECKED_CAST")
        val next = current.getOrPut(part) { mutableMapOf<String, Any>() } as MutableMap<String, Any>
        current = next
      }
      current[parts.last()] = value
    }
    return result
  }
}
