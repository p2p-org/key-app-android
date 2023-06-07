package org.p2p.wallet.striga.signup.validation

import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.emptyString
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.parser.CountryCodeXmlParser
import org.p2p.wallet.auth.repository.CountryCodeInMemoryRepository
import org.p2p.wallet.auth.repository.CountryCodeLocalRepository

class PhoneNumberInputValidatorTest {

    @MockK(relaxed = true)
    lateinit var countryCodeRepository: CountryCodeLocalRepository

    @Test
    fun `GIVEN valid phone number WHEN validate THEN return true`() {

        // Given
        val phoneNumber = "996709739641"
        val regionCode = "kg"

        val validator = PhoneNumberInputValidator(
            phoneNumber = phoneNumber,
            regionCode = regionCode,
            countryCodeRepository = countryCodeRepository
        )

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
        val regionCode = "kg"
        val validator = PhoneNumberInputValidator(
            phoneNumber = phoneNumber,
            regionCode = regionCode,
            countryCodeRepository = countryCodeRepository
        )

        // When
        val result = validator.validate(phoneNumber)

        // Then
        assertFalse(result)
        assertEquals(TextContainer(R.string.striga_validation_error_phone_number), validator.errorMessage)
    }

    @Test
    fun `GIVEN null input WHEN validate THEN return false`() {
        // Given
        val validator = PhoneNumberInputValidator(
            phoneNumber = emptyString(),
            regionCode = emptyString(),
            countryCodeRepository = countryCodeRepository
        )

        // When
        val result = validator.validate(null)

        // Then
        assertFalse(result)
        assertEquals(TextContainer(R.string.striga_validation_error_phone_number), validator.errorMessage)
    }

    @Test
    fun `GIVEN empty input WHEN validate THEN return false`() {
        // Given
        val validator = PhoneNumberInputValidator(
            phoneNumber = emptyString(),
            regionCode = emptyString(),
            countryCodeRepository = countryCodeRepository
        )

        // When
        val result = validator.validate("")

        // Then
        assertFalse(result)
        assertEquals(TextContainer(R.string.striga_validation_error_phone_number), validator.errorMessage)
    }
}
