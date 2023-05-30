package org.p2p.wallet.striga.signup.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.p2p.core.common.TextContainer
import org.p2p.wallet.R

class EmptyInputValidatorTest {

    @Test
    fun `GIVEN non-empty input WHEN validate THEN return true`() {
        // Given
        val validator = EmptyInputValidator()

        // When
        val result = validator.validate("Hello")

        // Then
        val errorMessage = validator.errorMessage as TextContainer.Res
        assertTrue(result)
        assertEquals(R.string.striga_validation_error_empty, errorMessage.textRes)
    }

    @Test
    fun `GIVEN empty input WHEN validate THEN return false`() {
        // Given
        val validator = EmptyInputValidator()

        // When
        val result = validator.validate("")

        // Then
        val errorMessage = validator.errorMessage as TextContainer.Res
        assertFalse(result)
        assertEquals(R.string.striga_validation_error_empty, errorMessage.textRes)
    }

    @Test
    fun `GIVEN blank input WHEN validate THEN return false`() {
        // Given
        val validator = EmptyInputValidator()

        // When
        val result = validator.validate("   ")

        // Then
        val errorMessage = validator.errorMessage as TextContainer.Res
        assertFalse(result)
        assertEquals(R.string.striga_validation_error_empty, errorMessage.textRes)
    }
}
