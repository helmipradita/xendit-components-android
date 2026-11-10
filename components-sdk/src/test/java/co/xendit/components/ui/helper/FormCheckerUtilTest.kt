package co.xendit.components.ui.helper

import co.xendit.components.ui.helper.FormCheckerUtil.isValidEmail
import com.google.i18n.phonenumbers.PhoneNumberUtil
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class FormCheckerUtilTest {

  @Test
  fun isValidCreditCard_validCards_returnsTrue() {
    // Test with some known valid card numbers (using Luhn algorithm)
    assertTrue(FormCheckerUtil.isValidCreditCard("378282246310005"))
    assertTrue(FormCheckerUtil.isValidCreditCard("3782 8224 6310 005"))
  }

  @Test
  fun isValidCreditCard_invalidCards_returnsFalse() {
    assertFalse(FormCheckerUtil.isValidCreditCard("49927398717")) // Invalid Luhn
    assertFalse(FormCheckerUtil.isValidCreditCard("123")) // Too short
    assertFalse(FormCheckerUtil.isValidCreditCard("12345678901234567890")) // Too long
    assertFalse(FormCheckerUtil.isValidCreditCard("")) // Empty
  }

  @Test
  fun isValidCardExpiry_validFutureDates_returnsTrue() {
    // Use "MMyy" to match your new 4-digit requirement
    val formatter = DateTimeFormatter.ofPattern("MMyy")

    val nextMonth = YearMonth.now().plusMonths(1)
    assertTrue(FormCheckerUtil.isValidCardExpiry(nextMonth.format(formatter)))

    val currentMonth = YearMonth.now()
    assertTrue(FormCheckerUtil.isValidCardExpiry(currentMonth.format(formatter)))
  }

  @Test
  fun isValidCardExpiry_pastDates_returnsFalse() {
    val formatter = DateTimeFormatter.ofPattern("MMyy")

    val lastMonth = YearMonth.now().minusMonths(1)
    assertFalse(FormCheckerUtil.isValidCardExpiry(lastMonth.format(formatter)))
  }

  @Test
  fun isValidCardExpiry_invalidFormats_returnsFalse() {
    // Invalid Month
    assertFalse(FormCheckerUtil.isValidCardExpiry("1325"))

    // Wrong digit counts
    assertFalse(FormCheckerUtil.isValidCardExpiry("125"))    // 3 digits
    assertFalse(FormCheckerUtil.isValidCardExpiry("012025")) // 6 digits

    // Non-numeric
    assertFalse(FormCheckerUtil.isValidCardExpiry("abcd"))
  }

  @Test
  fun isValidEmail_validFormats_returnsTrue() {
    assertTrue(isValidEmail("user@example.com"))
    assertTrue(isValidEmail("first.last@sub.domain.org"))
    assertTrue(isValidEmail("user123@gmail.co"))
  }

  @Test
  fun isValidEmail_invalidFormats_returnsFalse() {
    // Missing @
    assertFalse(isValidEmail("userexample.com"))

    // Missing domain
    assertFalse(isValidEmail("user@"))

    // Missing username
    assertFalse(isValidEmail("@example.com"))

    // Missing TLD (Top Level Domain)
    assertFalse(isValidEmail("user@example"))

    // Contains invalid characters/spaces
    assertFalse(isValidEmail("user name@example.com"))
  }

  @Test
  fun isValidPhoneNumber_validExampleNumber_returnsTrue() {
    val phoneUtil = PhoneNumberUtil.getInstance()
    val example = phoneUtil.getExampleNumber("ID")
    val nationalNumber = example.nationalNumber.toString()
    assertTrue(FormCheckerUtil.isValidPhoneNumber(nationalNumber, "ID"))
  }

  @Test
  fun isValidPhoneNumber_invalidNumber_returnsFalse() {
    assertFalse(FormCheckerUtil.isValidPhoneNumber("123", "ID"))
    assertFalse(FormCheckerUtil.isValidPhoneNumber("", "ID"))
  }

  @Test
  fun isValidPhoneNumber_normalizesRegionCodeWithFixedLocale() {
    val original = Locale.getDefault()
    try {
      Locale.setDefault(Locale.Builder().setLanguage("tr").setRegion("TR").build())

      val phoneUtil = PhoneNumberUtil.getInstance()
      val example = phoneUtil.getExampleNumber("ID")
      val nationalNumber = example.nationalNumber.toString()

      assertTrue(FormCheckerUtil.isValidPhoneNumber(nationalNumber, "id"))
    } finally {
      Locale.setDefault(original)
    }
  }

  @Test
  fun isValidPhoneNumber_internationalNumberWithEmptyOrDifferentRegion_returnsTrue() {
    val phoneUtil = PhoneNumberUtil.getInstance()
    val exampleUs = phoneUtil.getExampleNumber("US")
    val usE164 = phoneUtil.format(exampleUs, PhoneNumberUtil.PhoneNumberFormat.E164)

    // With empty regionCode
    assertTrue(FormCheckerUtil.isValidPhoneNumber(usE164, ""))

    // With different regionCode
    assertTrue(FormCheckerUtil.isValidPhoneNumber(usE164, "ID"))
  }

  @Test
  fun formatAmount_returnsEmpty_whenAmountOrCurrencyMissing() {
    assertEquals("", CurrencyUtil.formatAmount(null, "IDR"))
    assertEquals("", CurrencyUtil.formatAmount(BigDecimal.valueOf(1000), null))
    assertEquals("", CurrencyUtil.formatAmount(BigDecimal.valueOf(1000), ""))
  }

  @Test
  fun formatAmount_formatsKnownCurrencies_withIndonesianGrouping() {
    assertEquals("Rp300.000", CurrencyUtil.formatAmount(BigDecimal.valueOf(300_000), "IDR"))
    assertEquals("$300.000", CurrencyUtil.formatAmount(BigDecimal.valueOf(300_000), "USD"))
    assertEquals("₱300.000", CurrencyUtil.formatAmount(BigDecimal.valueOf(300_000), "PHP"))
  }

  @Test
  fun formatAmount_fallsBackToCurrencyCode_whenUnknownCurrency() {
    assertEquals("EUR300.000", CurrencyUtil.formatAmount(BigDecimal.valueOf(300_000), "EUR"))
  }
}
