package co.xendit.components.ui.helper

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.time.YearMonth
import java.util.Locale

internal object FormCheckerUtil {
  fun isValidCreditCard(cardNumber: String): Boolean {
    val digits = cardNumber.replace(Regex("\\D"), "")

    if (digits.length < 13 || digits.length > 19) return false

    var sum = 0
    var isSecond = false

    for (i in digits.length - 1 downTo 0) {
      var d = digits[i] - '0' // Convert Char to Int

      if (isSecond) {
        d *= 2
        if (d > 9) d -= 9
      }

      sum += d
      isSecond = !isSecond
    }

    return sum % 10 == 0
  }

  fun isValidCardExpiry(expiryData: String): Boolean {
    val digits = expiryData.replace(Regex("\\D"), "")
    if (digits.length != 4) return false

    val month = digits.substring(0, 2).toIntOrNull() ?: return false
    val yearShort = digits.substring(2, 4).toIntOrNull() ?: return false

    if (month !in 1..12) return false

    return try {
      val currentMonth = YearMonth.now()
      val expiry = YearMonth.of(2000 + yearShort, month)

      !expiry.isBefore(currentMonth)
    } catch (e: Exception) {
      false
    }
  }

  fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return email.matches(emailRegex)
  }

  fun isValidPhoneNumber(phoneNumber: String, regionCode: String): Boolean {
    val trimmed = phoneNumber.trim()
    if (trimmed.isBlank()) return false

    val normalizedRegion = regionCode.trim().uppercase(Locale.US)
    val phoneUtil = PhoneNumberUtil.getInstance()
    return try {
      val parsed = phoneUtil.parse(trimmed, normalizedRegion)
      if (normalizedRegion.isEmpty()) {
        phoneUtil.isValidNumber(parsed)
      } else {
        phoneUtil.isValidNumberForRegion(parsed, normalizedRegion) ||
          (trimmed.startsWith("+") && phoneUtil.isValidNumber(parsed))
      }
    } catch (_: NumberParseException) {
      false
    } catch (_: Exception) {
      false
    }
  }
}
