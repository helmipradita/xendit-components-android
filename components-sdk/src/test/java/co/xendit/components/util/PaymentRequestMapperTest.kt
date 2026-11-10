package co.xendit.components.util

import co.xendit.components.data.encryption.XenditEncryption
import co.xendit.components.data.model.ChannelFormField
import co.xendit.components.data.model.FieldType
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PaymentRequestMapperTest {

  @Before
  fun setUp() {
    mockkObject(XenditEncryption)
    every { XenditEncryption.encrypt(any(), any(), any()) } returns "encrypted_value"
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun mapFormValuesToChannelProperties_unflattensCorrectly() {
    val fields =
      listOf(
        ChannelFormField(
          label = "Card Number",
          groupLabel = null,
          placeholder = "1234...",
          type = FieldType("credit_card_number", null, null, true, null, null),
          channelProperty = "card_details.card_number",
          initialValue = null,
          disabled = false,
          required = true,
          span = 1,
          join = false,
          flags = null
        )
      )
    val formValues = mapOf("card_details.card_number" to "1234567812345678")

    val result =
      PaymentRequestMapper.mapFormValuesToChannelProperties(
        formValues,
        fields,
        "pub_key",
        "session_id"
      )

    val cardDetails = result["card_details"] as Map<*, *>
    assertEquals("encrypted_value", cardDetails["card_number"])
  }

  @Test
  fun mapFormValuesToChannelProperties_handlesExpirySplit() {
    val fields =
      listOf(
        ChannelFormField(
          label = "Expiry",
          groupLabel = null,
          placeholder = "MM/YY",
          type = FieldType("credit_card_expiry", null, null, true, null, null),
          channelProperty =
            listOf("card_details.expiry_month", "card_details.expiry_year"),
          initialValue = null,
          disabled = false,
          required = true,
          span = 1,
          join = false,
          flags = null
        )
      )
    val formValues = mapOf("card_details.expiry_month" to "1225")

    val result =
      PaymentRequestMapper.mapFormValuesToChannelProperties(
        formValues,
        fields,
        "pub_key",
        "session_id"
      )

    val cardDetails = result["card_details"] as Map<*, *>
    assertEquals("encrypted_value", cardDetails["expiry_month"])
    assertEquals("encrypted_value", cardDetails["expiry_year"])
  }

  @Test
  fun mapFormValuesToChannelProperties_prependsPlusToPhoneNumber() {
    val fields =
      listOf(
        ChannelFormField(
          label = "Mobile Number",
          groupLabel = null,
          placeholder = "8000032341",
          type = FieldType("phone_number", null, null, null, null, null),
          channelProperty = "card_details.cardholder_phone_number",
          initialValue = null,
          disabled = false,
          required = true,
          span = 2,
          join = false,
          flags = null
        )
      )
    val formValues = mapOf(
      "card_details.cardholder_phone_number" to "8000032341",
      "card_details.cardholder_phone_number_country_code" to "US"
    )

    val result =
      PaymentRequestMapper.mapFormValuesToChannelProperties(
        formValues,
        fields,
        "pub_key",
        "session_id"
      )

    val cardDetails = result["card_details"] as Map<*, *>
    assertEquals("+18000032341", cardDetails["cardholder_phone_number"])
  }

  @Test
  fun mapFormValuesToChannelProperties_doesNotPrependPlusIfAlreadyPresent() {
    val fields =
      listOf(
        ChannelFormField(
          label = "Mobile Number",
          groupLabel = null,
          placeholder = "8000032341",
          type = FieldType("phone_number", null, null, null, null, null),
          channelProperty = "card_details.cardholder_phone_number",
          initialValue = null,
          disabled = false,
          required = true,
          span = 2,
          join = false,
          flags = null
        )
      )
    val formValues = mapOf(
      "card_details.cardholder_phone_number" to "+18000032341",
      "card_details.cardholder_phone_number_country_code" to "US"
    )

    val result =
      PaymentRequestMapper.mapFormValuesToChannelProperties(
        formValues,
        fields,
        "pub_key",
        "session_id"
      )

    val cardDetails = result["card_details"] as Map<*, *>
    assertEquals("+18000032341", cardDetails["cardholder_phone_number"])
  }

  @Test
  fun mapFormValuesToChannelProperties_avoidsDuplicateDialCodeWhenEnteredWithoutPlus() {
    val fields =
      listOf(
        ChannelFormField(
          label = "Mobile Number",
          groupLabel = null,
          placeholder = "8000032341",
          type = FieldType("phone_number", null, null, null, null, null),
          channelProperty = "card_details.cardholder_phone_number",
          initialValue = null,
          disabled = false,
          required = true,
          span = 2,
          join = false,
          flags = null
        )
      )
    val formValues = mapOf(
      "card_details.cardholder_phone_number" to "18000032341",
      "card_details.cardholder_phone_number_country_code" to "US"
    )

    val result =
      PaymentRequestMapper.mapFormValuesToChannelProperties(
        formValues,
        fields,
        "pub_key",
        "session_id"
      )

    val cardDetails = result["card_details"] as Map<*, *>
    assertEquals("+18000032341", cardDetails["cardholder_phone_number"])
  }

  @Test
  fun mapFormValuesToChannelProperties_prependsPlusWhenCountryCodeMissing() {
    val fields =
      listOf(
        ChannelFormField(
          label = "Mobile Number",
          groupLabel = null,
          placeholder = "8000032341",
          type = FieldType("phone_number", null, null, null, null, null),
          channelProperty = "card_details.cardholder_phone_number",
          initialValue = null,
          disabled = false,
          required = true,
          span = 2,
          join = false,
          flags = null
        )
      )
    val formValues = mapOf("card_details.cardholder_phone_number" to "8000032341")

    val result =
      PaymentRequestMapper.mapFormValuesToChannelProperties(
        formValues,
        fields,
        "pub_key",
        "session_id"
      )

    val cardDetails = result["card_details"] as Map<*, *>
    assertEquals("+8000032341", cardDetails["cardholder_phone_number"])
  }

  @Test
  fun mapFormValuesToChannelProperties_normalizesBracketArrayKeyToList() {
    val fields =
      listOf(
        ChannelFormField(
          label = "Fund Source",
          groupLabel = null,
          placeholder = "CASA",
          type = FieldType("text", null, null, null, null, null),
          channelProperty = "fund_source[]",
          initialValue = null,
          disabled = false,
          required = true,
          span = 1,
          join = false,
          flags = null
        )
      )
    val formValues = mapOf("fund_source[]" to "CASA")

    val result =
      PaymentRequestMapper.mapFormValuesToChannelProperties(
        formValues,
        fields,
        "pub_key",
        "session_id"
      )

    assertEquals(listOf("CASA"), result["fund_source"])
    assertEquals(null, result["fund_source[]"])
  }

  @Test
  fun mapFormValuesToChannelProperties_normalizesBracketArrayKeyToList_nestedKey() {
    val fields =
      listOf(
        ChannelFormField(
          label = "Fund Source",
          groupLabel = null,
          placeholder = "CASA",
          type = FieldType("text", null, null, null, null, null),
          channelProperty = "payment_details.fund_source[]",
          initialValue = null,
          disabled = false,
          required = true,
          span = 1,
          join = false,
          flags = null
        )
      )
    val formValues = mapOf("payment_details.fund_source[]" to "CASA")

    val result =
      PaymentRequestMapper.mapFormValuesToChannelProperties(
        formValues,
        fields,
        "pub_key",
        "session_id"
      )

    val paymentDetails = result["payment_details"] as Map<*, *>
    assertEquals(listOf("CASA"), paymentDetails["fund_source"])
    assertEquals(null, paymentDetails["fund_source[]"])
  }
}
