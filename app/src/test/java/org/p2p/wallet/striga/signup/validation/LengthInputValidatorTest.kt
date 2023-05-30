package org.p2p.wallet.striga.signup.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.p2p.core.common.TextContainer
import org.p2p.wallet.R

class LengthInputValidatorTest {

    @Test
    fun `GIVEN input with length greater than minimum length WHEN validate THEN return true`() {
        // Given
        val minLength = 3
        val validator = LengthInputValidator(minLength)

        // When
        val result = validator.validate("Hello")

        // Then
        assertTrue(result)
        assertEquals(TextContainer(R.string.striga_validation_error_too_short), validator.errorMessage)
    }

    @Test
    fun `GIVEN input with length equal to minimum length WHEN validate THEN return false`() {
        // Given
        val minLength = 5
        val validator = LengthInputValidator(minLength)

        // When
        val result = validator.validate("World")

        // Then
        assertFalse(result)
        assertEquals(TextContainer(R.string.striga_validation_error_too_short), validator.errorMessage)
    }

    @Test
    fun `GIVEN input with length less than minimum length WHEN validate THEN return false`() {
        // Given
        val minLength = 8
        val validator = LengthInputValidator(minLength)

        // When
        val result = validator.validate("Hi")

        // Then
        assertFalse(result)
        assertEquals(TextContainer(R.string.striga_validation_error_too_short), validator.errorMessage)
    }

    @Test
    fun `GIVEN null input WHEN validate THEN return false`() {
        // Given
        val minLength = 1
        val validator = LengthInputValidator(minLength)

        // When
        val result = validator.validate(null)

        // Then
        assertFalse(result)
        assertEquals(TextContainer(R.string.striga_validation_error_too_short), validator.errorMessage)
    }

    @Test
    fun `GIVEN empty input WHEN validate THEN return false`() {
        // Given
        val minLength = 1
        val validator = LengthInputValidator(minLength)

        // When
        val result = validator.validate("")

        // Then
        assertFalse(result)
        assertEquals(TextContainer(R.string.striga_validation_error_too_short), validator.errorMessage)
    }
}
