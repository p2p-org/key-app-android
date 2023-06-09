package org.p2p.wallet.striga.signup.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.p2p.core.common.TextContainer
import org.p2p.wallet.R

class LengthInputValidatorTest {

    @Test
    fun `GIVEN input with length within range WHEN validate THEN return true`() {
        val validator = LengthInputValidator(minLength = 2, maxLength = 5)
        val input = "abc"
        val result = validator.validate(input)
        assertTrue(result)
    }

    @Test
    fun `GIVEN input with length within range WHEN validate for range THEN return true`() {
        val validator = LengthInputValidator(2..5)
        val input = "abc"
        val result = validator.validate(input)
        assertTrue(result)
    }

    @Test
    fun `GIVEN input with maximum length within range WHEN validate for range THEN return true`() {
        val validator = LengthInputValidator(2..5)
        val input = "abcde"
        val result = validator.validate(input)
        assertTrue(result)
    }

    @Test
    fun `GIVEN input with length less than minLength WHEN validate THEN return false with appropriate error message`() {
        val validator = LengthInputValidator(minLength = 2, maxLength = 5)
        val input = "a"
        val result = validator.validate(input)
        assertFalse(result)
        val errorMessage = validator.errorMessage as TextContainer.Res
        assertEquals(R.string.striga_validation_error_too_short, errorMessage.textRes)
    }

    @Test
    fun `GIVEN input with length greater than maxLength WHEN validate THEN return false with appropriate error message`() {
        val validator = LengthInputValidator(minLength = 2, maxLength = 5)
        val input = "abcdefg"
        val result = validator.validate(input)
        assertFalse(result)
        val errorMessage = validator.errorMessage as TextContainer.Res
        assertEquals(R.string.striga_validation_error_too_long, errorMessage.textRes)
    }

    @Test
    fun `GIVEN null input WHEN validate THEN return false with appropriate error message`() {
        val validator = LengthInputValidator(minLength = 2)
        val input: String? = null
        val result = validator.validate(input)
        assertFalse(result)
        val errorMessage = validator.errorMessage as TextContainer.Res
        assertEquals(R.string.striga_validation_error_too_short, errorMessage.textRes)
    }

    @Test
    fun `GIVEN input with length within range and maxLength not set WHEN validate THEN return true`() {
        val validator = LengthInputValidator(minLength = 2)
        val input = "abc"
        val result = validator.validate(input)
        assertTrue(result)
    }
}
