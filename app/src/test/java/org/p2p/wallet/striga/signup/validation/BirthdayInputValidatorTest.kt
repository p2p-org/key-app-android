package org.p2p.wallet.striga.signup.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.p2p.core.common.TextContainer
import org.p2p.wallet.R

@ExperimentalCoroutinesApi
class BirthdayInputValidatorTest {

    @Test
    fun `GIVEN valid birthday WHEN validate THEN return true`() {
        // Given
        val validator = BirthdayInputValidator()

        // When
        val result = validator.validate("01.01.1990")

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN invalid birthday WHEN validate THEN return false`() {
        // Given
        val validator = BirthdayInputValidator()

        // When
        val result = validator.validate("05.30.1995")

        // Then
        assertFalse(result)
    }

    @Test
    fun `GIVEN birthday older than minimum year WHEN validate THEN return false with specific error message`() {
        // Given
        val validator = BirthdayInputValidator()

        // When
        val result = validator.validate("01.01.1910")

        // Then
        val errorMessage = (validator.errorMessage) as TextContainer.ResParams
        assertFalse(result)
        assertEquals(R.string.striga_validation_error_wrong_birthday_older_than, errorMessage.textRes)
        assertEquals(1920, errorMessage.args[0])
    }

    @Test
    fun `GIVEN birthday younger than maximum year WHEN validate THEN return false with specific error message`() {
        // Given
        val validator = BirthdayInputValidator()

        // When
        val result = validator.validate("01.01.2025")

        // Then
        val errorMessage = (validator.errorMessage) as TextContainer.ResParams
        assertFalse(result)
        assertEquals(R.string.striga_validation_error_wrong_birthday_younger_than, errorMessage.textRes)
        assertEquals(2015, errorMessage.args[0])
    }
}
