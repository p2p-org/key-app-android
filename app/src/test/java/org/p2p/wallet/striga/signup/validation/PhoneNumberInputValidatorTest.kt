package org.p2p.wallet.striga.signup.validation

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import java.io.File
import java.io.InputStream
import java.nio.file.Paths
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.emptyString
import org.p2p.wallet.R
import org.p2p.wallet.auth.gateway.parser.CountryCodeXmlParser
import org.p2p.wallet.auth.repository.CountryCodeInMemoryRepository
import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.utils.TestCoroutineDispatchers
import org.p2p.wallet.utils.TimberUnitTestInstance

class PhoneNumberInputValidatorTest {

    companion object {
        @ClassRule
        @JvmField
        val timber = TimberUnitTestInstance(
            isEnabled = false,
            defaultTag = "PhoneNumberInputValidatorTest"
        )
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        initCountryCodeLocalRepository()
    }

    private val currentWorkingDir = Paths.get("").toAbsolutePath().toString()
    private val assetsRoot = File(currentWorkingDir, "build/intermediates/assets/debug")
    private val dispatchers = TestCoroutineDispatchers()
    lateinit var countryCodeRepository: CountryCodeRepository

    @Test
    fun `GIVEN valid phone number WHEN validate THEN return true`() {

        // Given
        val phoneNumber = "996709739641"
        val regionCode = "kg"

        val validator = PhoneNumberInputValidator(
            regionCodeAlpha2 = regionCode,
            countryCodeRepository = countryCodeRepository
        )

        // When
        val result = validator.validate(phoneNumber)

        // Then
        assertTrue(result)
        assertEquals(TextContainer(R.string.striga_validation_error_phone_number), validator.errorMessage)
    }

    @Test
    fun `GIVEN valid US phone number WHEN validate THEN return true`() {
        // Given
        val validator = PhoneNumberInputValidator(
            regionCodeAlpha2 = "US",
            countryCodeRepository = countryCodeRepository
        )

        // When
        val result = validator.validate("555 55 55")

        // Then
        assertFalse(result)
        assertEquals(TextContainer(R.string.striga_validation_error_phone_number), validator.errorMessage)
    }

    @Test
    fun `GIVEN invalid US phone number WHEN validate THEN return true`() {
        // Given
        val validator = PhoneNumberInputValidator(
            regionCodeAlpha2 = "US",
            countryCodeRepository = countryCodeRepository
        )

        // When
        val result = validator.validate("555 55 55 55")

        // Then
        assertFalse(result)
        assertEquals(TextContainer(R.string.striga_validation_error_phone_number), validator.errorMessage)
    }

    @Test
    fun `GIVEN invalid phone number WHEN validate THEN return false`() {
        // Given
        val phoneNumber = "12345"
        val regionCode = "kg"
        val validator = PhoneNumberInputValidator(
            regionCodeAlpha2 = regionCode,
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
            regionCodeAlpha2 = emptyString(),
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
            regionCodeAlpha2 = emptyString(),
            countryCodeRepository = countryCodeRepository
        )

        // When
        val result = validator.validate("")

        // Then
        assertFalse(result)
        assertEquals(TextContainer(R.string.striga_validation_error_phone_number), validator.errorMessage)
    }

    private fun initCountryCodeLocalRepository() {
        val assetManager: AssetManager = mockk {
            every { open(any()) } answers {
                val file = File(assetsRoot, arg<String>(0))
                if (!file.exists()) error("File not found: ${file.absolutePath}")
                file.inputStream()
            }
        }

        val context = mockk<Context> {
            every { assets } returns assetManager
        }
        val resources = mockk<Resources> {
            every { openRawResource(R.raw.ccp_english) } returns readCountriesXmlFile()
        }
        val phoneNumberUtil = PhoneNumberUtil.createInstance(context)

        val parser = CountryCodeXmlParser(resources, phoneNumberUtil)

        countryCodeRepository = CountryCodeInMemoryRepository(
            dispatchers = dispatchers,
            context = context,
            countryCodeHelper = parser
        )
    }

    private fun readCountriesXmlFile(): InputStream {
        val ccpEnglishFile = File(currentWorkingDir, "src/main/res/raw/ccp_english.xml")
        assertTrue(ccpEnglishFile.exists())

        return ccpEnglishFile.inputStream()
    }
}
