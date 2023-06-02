package org.p2p.wallet.striga.signup.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.p2p.core.common.TextContainer
import org.p2p.wallet.R

class PhoneNumberInputValidatorTest {

    @Test
    fun `GIVEN valid phone number WHEN validate THEN return true`() {
        // Given
        val phoneNumber = "+90 555 666 77 88"
        val validator = PhoneNumberInputValidator()

        // When
        val result = validator.validate(phoneNumber)

        // Then
        assertTrue(result)
        assertEquals(TextContainer(R.string.striga_validation_error_phone_number), validator.errorMessage)
    }

    @Test
    fun `GIVEN invalid phone number WHEN validate THEN return false`() {
        // Given
        val phoneNumber = "12345"
        val validator = PhoneNumberInputValidator()

        // When
        val result = validator.validate(phoneNumber)

        // Then
        assertFalse(result)
        assertEquals(TextContainer(R.string.striga_validation_error_phone_number), validator.errorMessage)
    }

    @Test
    fun `GIVEN null input WHEN validate THEN return false`() {
        // Given
        val validator = PhoneNumberInputValidator()

        // When
        val result = validator.validate(null)

        // Then
        assertFalse(result)
        assertEquals(TextContainer(R.string.striga_validation_error_phone_number), validator.errorMessage)
    }

    @Test
    fun `GIVEN empty input WHEN validate THEN return false`() {
        // Given
        val validator = PhoneNumberInputValidator()

        // When
        val result = validator.validate("")

        // Then
        assertFalse(result)
        assertEquals(TextContainer(R.string.striga_validation_error_phone_number), validator.errorMessage)
    }
}
