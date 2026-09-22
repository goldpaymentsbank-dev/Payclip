package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.spei.SpeiEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SPEI Reels", appName)
  }

  @Test
  fun `test spei clabe modulo 10 validation`() {
    // 012180015498263828 is a valid BBVA CLABE with check digit 8
    val resultValid = SpeiEngine.validateClabe("012180015498263828")
    assertTrue(resultValid.isValid)
    assertEquals("BBVA México", resultValid.bankName)
    assertEquals(8, resultValid.calculatedCheckDigit)

    // Test invalid length
    val resultInvalidLength = SpeiEngine.validateClabe("12345")
    assertFalse(resultInvalidLength.isValid)

    // Test invalid check digit (e.g. ending in 9 instead of 8)
    val resultInvalidCheck = SpeiEngine.validateClabe("012180015498263829")
    assertFalse(resultInvalidCheck.isValid)
  }

  @Test
  fun `test concept sanitization for banxico spei`() {
    val raw = "Premio Reto! #102: ¡Felicidades! @Doritos"
    val sanitized = SpeiEngine.sanitizeConcept(raw)
    assertEquals("PREMIO RETO 102 FELICIDADES DORITOS", sanitized)
    assertTrue(sanitized.length <= 40)
  }
}
