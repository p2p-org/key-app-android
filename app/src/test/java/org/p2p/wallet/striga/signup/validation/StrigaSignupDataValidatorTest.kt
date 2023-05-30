package org.p2p.wallet.striga.signup.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType

class StrigaSignupDataValidatorTest {

    private var validator = StrigaSignupDataValidator()

    @BeforeEach
    fun beforeEach() {
        validator = StrigaSignupDataValidator()
    }

    @Test
    fun `GIVEN valid phone number WHEN validating data THEN field state is valid`() {
        // Arrange
        val data = StrigaSignupData(StrigaSignupDataType.PHONE_NUMBER, "+1234567890")

        // Act
        val fieldState = validator.validate(data)

        // Assert
        assertTrue(fieldState.isValid)
        assertNull(fieldState.errorMessage)
    }

    @Test
    fun `GIVEN invalid phone number WHEN validating data THEN field state is invalid`() {
        // Arrange
        val data = StrigaSignupData(StrigaSignupDataType.PHONE_NUMBER, "12345")

        // Act
        val fieldState = validator.validate(data)

        // Assert
        assertFalse(fieldState.isValid)
        assertNotNull(fieldState.errorMessage)
    }

    @Test
    fun `GIVEN valid first name WHEN validating data THEN field state is valid`() {
        // Arrange
        val data = StrigaSignupData(StrigaSignupDataType.FIRST_NAME, "John")

        // Act
        val fieldState = validator.validate(data)

        // Assert
        assertTrue(fieldState.isValid)
        assertNull(fieldState.errorMessage)
    }

    @Test
    fun `GIVEN empty first name WHEN validating data THEN field state is invalid`() {
        // Arrange
        val data = StrigaSignupData(StrigaSignupDataType.FIRST_NAME, "")

        // Act
        val fieldState = validator.validate(data)

        // Assert
        assertFalse(fieldState.isValid)
        assertNotNull(fieldState.errorMessage)
    }
}
