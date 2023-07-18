package org.p2p.wallet.striga.signup.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.p2p.core.common.TextContainer
import org.p2p.wallet.R
import org.p2p.wallet.striga.signup.steps.validation.BirthdayInputValidator

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
    fun `GIVEN invalid month WHEN validate THEN check invalid month error`() {
        // Given
        val validator = BirthdayInputValidator()

        // When
        val result = validator.validate("01.30.1990")

        // Then
        val errorMessage = validator.errorMessage as TextContainer.Res
        assertFalse(result)
        assertEquals(R.string.striga_validation_error_wrong_birthday_month, errorMessage.textRes)
    }

    @Test
    fun `GIVEN invalid day and month WHEN validate THEN check invalid day error`() {
        // Given
        val validator = BirthdayInputValidator()

        // When
        val result = validator.validate("99.99.1990")

        // Then
        val errorMessage = validator.errorMessage as TextContainer.Res
        assertFalse(result)
        assertEquals(R.string.striga_validation_error_wrong_birthday_day, errorMessage.textRes)
    }

    @Test
    fun `GIVEN random string WHEN validate THEN check invalid date format`() {
        // Given
        val validator = BirthdayInputValidator()

        // When
        val result = validator.validate("all hell breaks loose")

        // Then
        val errorMessage = validator.errorMessage as TextContainer.Res
        assertFalse(result)
        assertEquals(R.string.striga_validation_error_wrong_birthday_common, errorMessage.textRes)
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

    @Test
    fun `GIVEN birthday that is a maximum year WHEN validate THEN return true`() {
        // Given
        val validator = BirthdayInputValidator(1920, 2015)

        // When
        val result = validator.validate("01.01.2015")

        // Then
        assertTrue(result)
    }

    @Test
    fun `GIVEN birthday that is a minimum year WHEN validate THEN return true`() {
        // Given
        val validator = BirthdayInputValidator(1920, 2015)

        // When
        val result = validator.validate("01.01.1920")

        // Then
        assertTrue(result)
    }
}
