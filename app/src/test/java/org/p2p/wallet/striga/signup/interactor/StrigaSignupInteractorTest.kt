@file:OptIn(ExperimentalCoroutinesApi::class)

package org.p2p.wallet.striga.signup.interactor

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.core.utils.emptyString
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.model.PhoneNumberWithCode
import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.core.common.di.AppScope
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.model.toFailureResult
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.signup.validation.StrigaSignupDataValidator
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.striga.user.model.StrigaUserInitialDetails
import org.p2p.wallet.striga.user.model.StrigaUserInitialKycDetails
import org.p2p.wallet.striga.user.model.StrigaUserVerificationStatus
import org.p2p.wallet.utils.TestAppScope
import org.p2p.wallet.utils.UnconfinedTestDispatchers
import org.p2p.wallet.utils.createHttpException
import org.p2p.wallet.utils.mockInAppFeatureFlag

private val SupportedCountry = CountryCode(
    countryName = "United Kingdom",
    flagEmoji = "🇬🇧",
    nameCodeAlpha2 = "gb",
    nameCodeAlpha3 = "gbr",
    phoneCode = "123",
    mask = ""
)

private val TurkeyCountry = CountryCode(
    countryName = "Turkey",
    flagEmoji = "\uF1F9\uF1F7",
    nameCodeAlpha2 = "TR",
    nameCodeAlpha3 = "TUR",
    phoneCode = "90",
    mask = " 212 345 67 89"
)

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaSignupInteractorTest {

    @MockK
    lateinit var signupDataRepository: StrigaSignupDataLocalRepository

    @MockK
    lateinit var countryRepository: CountryCodeRepository

    @MockK
    lateinit var userInteractor: StrigaUserInteractor

    @MockK
    lateinit var metadataInteractor: MetadataInteractor

    @MockK(relaxed = true)
    lateinit var inAppFeatureFlags: InAppFeatureFlags

    private val signupDataValidator = StrigaSignupDataValidator()

    private val dispatchers: CoroutineDispatchers = UnconfinedTestDispatchers()
    private val appScope: AppScope = TestAppScope(dispatchers.ui)

    private val savedSignupDataStorage = mutableListOf<StrigaSignupData>()

    private var firstStepData = mutableMapOf<StrigaSignupDataType, StrigaSignupData>()
    private var secondStepData = mutableMapOf<StrigaSignupDataType, StrigaSignupData>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        every { inAppFeatureFlags.strigaSimulateWeb3Flag } returns mockInAppFeatureFlag(false)
        every { inAppFeatureFlags.strigaSimulateUserCreateFlag } returns mockInAppFeatureFlag(false)
        every { inAppFeatureFlags.strigaSimulateMobileAlreadyVerifiedFlag } returns mockInAppFeatureFlag(false)

        coEvery { countryRepository.detectCountryOrDefault() } returns SupportedCountry
    }

    @BeforeEach
    fun beforeEach() {
        firstStepData.clear()
        secondStepData.clear()
    }

    @Test
    fun `GIVEN error WHEN getSignupData THEN check returned signup data list is empty`() = runTest {
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Failure(
            StrigaDataLayerError.InternalError(IllegalStateException("Known exception"))
        )

        val interactor = createInteractor()
        val resultFirst = interactor.getSignupDataFirstStep()
        val resultSecond = interactor.getSignupDataSecondStep()
        assertEquals(0, resultFirst.size)
        assertEquals(0, resultSecond.size)
    }

    @Test
    fun `GIVEN signup data WHEN notify data changed and saveChanges THEN data is updated`() = runTest {
        // GIVEN
        initSignupDataRepository(emptyList())
        val interactor = createInteractor()
        val data = mutableMapOf(
            StrigaSignupDataType.PHONE_NUMBER to StrigaSignupData(StrigaSignupDataType.PHONE_NUMBER, "1234567890"),
            StrigaSignupDataType.COUNTRY_ALPHA_2 to StrigaSignupData(StrigaSignupDataType.COUNTRY_ALPHA_2, "TR")
        )

        // WHEN
        interactor.saveChanges(data.values)

        // THEN
        val resultFirst = interactor.getSignupDataFirstStep()
        val resultSecond = interactor.getSignupDataFirstStep()
        assertEquals(1, resultFirst.size)
        assertEquals(1, resultSecond.size)
    }

    @Test
    fun `GIVEN invalid full signup data WHEN validate first step THEN validation result is invalid`() = runTest {
        // GIVEN
        initSignupDataRepository(emptyList())
        val interactor = createInteractor()

        // WHEN
        setData(StrigaSignupDataType.EMAIL, "1234567890")
        setData(StrigaSignupDataType.PHONE_NUMBER, "hello world")
        setData(StrigaSignupDataType.FIRST_NAME, "1234567890")
        setData(StrigaSignupDataType.LAST_NAME, "1234567890")
        setData(StrigaSignupDataType.DATE_OF_BIRTH, "1234567890")
        setData(StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3, "1234567890")
        val (isValid, _) = interactor.validateFirstStep(firstStepData)
        val (isValidSecond, _) = interactor.validateSecondStep(firstStepData)

        // THEN
        assertFalse(isValid)
        assertFalse(isValidSecond)
    }

    @Test
    fun `GIVEN valid full signup data WHEN validate first step THEN validation result is valid`() = runTest {
        // GIVEN
        initSignupDataRepository(emptyList())
        val interactor = createInteractor()

        // WHEN
        setData(StrigaSignupDataType.EMAIL, "aaa@bbb.ccc")
        setData(StrigaSignupDataType.PHONE_CODE_WITH_PLUS, "+1")
        setData(StrigaSignupDataType.PHONE_NUMBER, "+1234567890")
        setData(StrigaSignupDataType.FIRST_NAME, "Vasya")
        setData(StrigaSignupDataType.LAST_NAME, "Pupkin")
        setData(StrigaSignupDataType.DATE_OF_BIRTH, "10.10.2010")
        setData(StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3, "Somewhere")
        val (isValid, statesFirst) = interactor.validateFirstStep(firstStepData)
        val (isValidSecond, statesSecond) = interactor.validateSecondStep(secondStepData)

        val stepFirstError = if (!isValid) {
            statesFirst.first { !it.isValid }.type
        } else {
            null
        }
        val stepSecondError = if (!isValid) {
            statesSecond.first { !it.isValid }.type
        } else {
            null
        }

        // THEN
        assertTrue("First step is invalid: message id: $stepFirstError", isValid)
        assertFalse("First step is invalid: message id: $stepSecondError", isValidSecond)
    }

    @Test
    fun `GIVEN invalid full signup data WHEN validate second step THEN validation result is invalid`() = runTest {
        // GIVEN
        initSignupDataRepository(emptyList())
        val interactor = createInteractor()

        // WHEN
        setData(StrigaSignupDataType.OCCUPATION, "")
        setData(StrigaSignupDataType.SOURCE_OF_FUNDS, " world")
        setData(StrigaSignupDataType.COUNTRY_ALPHA_2, "")
        setData(StrigaSignupDataType.CITY, "")
        setData(StrigaSignupDataType.CITY_ADDRESS_LINE, "")
        setData(StrigaSignupDataType.CITY_POSTAL_CODE, "")
        setData(StrigaSignupDataType.CITY_STATE, "")
        val (isValid, _) = interactor.validateSecondStep(firstStepData)
        val (isValidFirst, _) = interactor.validateFirstStep(firstStepData)

        // THEN
        assertFalse(isValid)
        assertFalse(isValidFirst)
    }

    @Test
    fun `GIVEN valid full signup data WHEN validate second step THEN validation result is valid`() = runTest {
        // GIVEN
        initSignupDataRepository(emptyList())
        val interactor = createInteractor()

        // WHEN
        setData(StrigaSignupDataType.OCCUPATION, "a")
        setData(StrigaSignupDataType.SOURCE_OF_FUNDS, "a")
        setData(StrigaSignupDataType.COUNTRY_ALPHA_2, "a")
        setData(StrigaSignupDataType.CITY, "ab")
        setData(StrigaSignupDataType.CITY_ADDRESS_LINE, "a")
        setData(StrigaSignupDataType.CITY_POSTAL_CODE, "a")
        setData(StrigaSignupDataType.CITY_STATE, "a")
        val (isValid, _) = interactor.validateSecondStep(firstStepData)
        val (isValidFirst, _) = interactor.validateFirstStep(firstStepData)

        // THEN
        assertTrue(isValid)
        assertFalse(isValidFirst)
    }

    @Test
    fun `GIVEN some country WHEN getSelectedCountry THEN check returned value is mock`() = runTest {
        val interactor = createInteractor()
        val selectedCountry = interactor.getSelectedCountry()
        assertEquals(selectedCountry, SupportedCountry)
    }

    @Test
    fun `GIVEN signup data WHEN getUserSignupData with empty set THEN check all fields are present`() = runTest {
        initSignupDataRepository(emptyList())
        setData(StrigaSignupDataType.OCCUPATION, "a")
        setData(StrigaSignupDataType.SOURCE_OF_FUNDS, "a")
        setData(StrigaSignupDataType.COUNTRY_ALPHA_2, "a")
        setData(StrigaSignupDataType.CITY, "a")
        setData(StrigaSignupDataType.CITY_ADDRESS_LINE, "a")
        setData(StrigaSignupDataType.CITY_POSTAL_CODE, "a")
        setData(StrigaSignupDataType.CITY_STATE, "a")

        val interactor = createInteractor()
        interactor.saveChanges(firstStepData.values)
        interactor.saveChanges(secondStepData.values)

        val result = interactor.getSignupData(emptySet())
        assertEquals(savedSignupDataStorage.size, result.size)
        assertEquals(savedSignupDataStorage, result)
    }

    @Test
    fun `GIVEN signup data WHEN getUserSignupData with one selected field THEN check this field is present`() = runTest {
        initSignupDataRepository(emptyList())
        setData(StrigaSignupDataType.OCCUPATION, "a")
        setData(StrigaSignupDataType.SOURCE_OF_FUNDS, "b")
        setData(StrigaSignupDataType.COUNTRY_ALPHA_2, "c")
        setData(StrigaSignupDataType.CITY, "d")
        setData(StrigaSignupDataType.CITY_ADDRESS_LINE, "e")
        setData(StrigaSignupDataType.CITY_POSTAL_CODE, "f")
        setData(StrigaSignupDataType.CITY_STATE, "g")

        val interactor = createInteractor()
        interactor.saveChanges(firstStepData.values)
        interactor.saveChanges(secondStepData.values)

        val result = interactor.getSignupData(setOf(StrigaSignupDataType.OCCUPATION))
        assertEquals(1, result.size)
        assertEquals("a", result[0].value)
    }

    @Test
    fun `GIVEN unavailable metadata WHEN createUser THEN check exception`() = runTest {
        every { metadataInteractor.currentMetadata } returns null

        val interactor = createInteractor()
        assertThrows<IllegalStateException> {
            interactor.createUser()
        }
    }

    @Test
    fun `GIVEN striga api 500 error WHEN createUser THEN check exception`() = runTest {
        initSignupDataRepository(emptyList())
        every { metadataInteractor.currentMetadata } returns GatewayOnboardingMetadata(
            deviceShareDeviceName = "",
            customSharePhoneNumberE164 = "",
            socialShareOwnerEmail = "",
            ethPublic = null,
            metaTimestampSec = 0L,
            deviceNameTimestampSec = 0L,
            phoneNumberTimestampSec = 0L,
            emailTimestampSec = 0L,
            authProviderTimestampSec = 0L,
            strigaMetadata = null
        )

        val responseBody = """
            {
                "status": 500,
                "errorCode": 0
            }
        """.trimIndent()
        coEvery { userInteractor.createUser(any()) } answers {
            StrigaDataLayerError.ApiServiceUnavailable(
                createHttpException(500, responseBody)
            ).toFailureResult()
        }

        val interactor = createInteractor()
        assertThrows<StrigaDataLayerError.ApiServiceUnavailable> {
            interactor.createUser()
        }
    }

    @Test
    fun `GIVEN signup WHEN createUser THEN check data is saved to metadata`() = runTest {
        initSignupDataRepository(emptyList())
        every { metadataInteractor.currentMetadata } returns GatewayOnboardingMetadata(
            deviceShareDeviceName = "",
            customSharePhoneNumberE164 = "",
            socialShareOwnerEmail = "",
            ethPublic = null,
            metaTimestampSec = 0L,
            deviceNameTimestampSec = 0L,
            phoneNumberTimestampSec = 0L,
            emailTimestampSec = 0L,
            authProviderTimestampSec = 0L,
            strigaMetadata = null
        )

        coEvery { userInteractor.loadAndSaveUserStatusData() } returns StrigaDataLayerResult.Success(Unit)
        coEvery { userInteractor.createUser(any()) } returns StrigaDataLayerResult.Success(
            StrigaUserInitialDetails(
                userId = "userId",
                email = "email",
                kycStatus = StrigaUserInitialKycDetails(
                    status = StrigaUserVerificationStatus.NOT_STARTED,
                    isEmailVerified = false,
                    isMobileVerified = false
                )
            )
        )
        val updatedMetadataSlot = slot<GatewayOnboardingMetadata>()
        coEvery { metadataInteractor.updateMetadata(capture(updatedMetadataSlot)) } returns Unit

        val interactor = createInteractor()
        assertDoesNotThrow { interactor.createUser() }
        val updatedMetadata = updatedMetadataSlot.captured
        assertEquals("userId", updatedMetadata.strigaMetadata?.userId)

        // we don't get user status after creation anymore
        coVerify(exactly = 0) { userInteractor.loadAndSaveUserStatusData() }
    }

    @Test
    fun `GIVEN empty cache metadata WHEN retrievePhoneNumber THEN code is default and number is empty`() =
        runTest {
            every { metadataInteractor.currentMetadata } returns null
            coEvery { countryRepository.detectCountryOrDefault() } returns SupportedCountry

            val interactor = createInteractor()
            val result = interactor.retrievePhoneNumberWithCode(null, null)
            assertEquals(emptyString(), result.phoneNumberNational)
            assertEquals(SupportedCountry, result.phoneCode)
        }

    @Test
    fun `GIVEN empty cache and not empty metadata WHEN retrievePhoneNumber THEN code and number from metadata`() =
        runTest {
            val expectedCode = "+90"
            val expectedNumber = "5348558899"
            every { metadataInteractor.currentMetadata } returns GatewayOnboardingMetadata(
                deviceShareDeviceName = "",
                customSharePhoneNumberE164 = "${expectedCode}$expectedNumber",
                socialShareOwnerEmail = "",
                ethPublic = null,
                metaTimestampSec = 0L,
                deviceNameTimestampSec = 0L,
                phoneNumberTimestampSec = 0L,
                emailTimestampSec = 0L,
                authProviderTimestampSec = 0L,
                strigaMetadata = null
            )
            every { countryRepository.parsePhoneNumber(any(), any()) } returns PhoneNumberWithCode(TurkeyCountry, expectedNumber)
            coEvery { countryRepository.detectCountryOrDefault() } returns SupportedCountry

            val interactor = createInteractor()
            val result = interactor.retrievePhoneNumberWithCode(null, null)
            assertEquals(expectedNumber, result.phoneNumberNational)
            assertEquals(TurkeyCountry, result.phoneCode)
        }

    @Test
    fun `GIVEN not empty cache and not empty metadata WHEN retrievePhoneNumber THEN code and number from cache`() =
        runTest {
            val codeFromMetadata = "+90"
            val numberFromMetadata = "5348558800"
            val expectedCode = "+90"
            val expectedNumber = "5348558899"
            every { metadataInteractor.currentMetadata } returns GatewayOnboardingMetadata(
                deviceShareDeviceName = "",
                customSharePhoneNumberE164 = "${codeFromMetadata}$numberFromMetadata",
                socialShareOwnerEmail = "",
                ethPublic = null,
                metaTimestampSec = 0L,
                deviceNameTimestampSec = 0L,
                phoneNumberTimestampSec = 0L,
                emailTimestampSec = 0L,
                authProviderTimestampSec = 0L,
                strigaMetadata = null
            )

            every { countryRepository.findCountryCodeByPhoneCode("+90") } returns TurkeyCountry
            coEvery { countryRepository.detectCountryOrDefault() } returns SupportedCountry

            val interactor = createInteractor()
            val result = interactor.retrievePhoneNumberWithCode(cachedPhoneCode = expectedCode, cachedPhoneNumber = expectedNumber)
            assertNotEquals(numberFromMetadata, result.phoneNumberNational)
            assertEquals(expectedNumber, result.phoneNumberNational)
            assertEquals(TurkeyCountry, result.phoneCode)
        }

    private fun createInteractor(): StrigaSignupInteractor {
        return StrigaSignupInteractor(
            appScope = appScope,
            inAppFeatureFlags = inAppFeatureFlags,
            validator = signupDataValidator,
            countryCodeRepository = countryRepository,
            signupDataRepository = signupDataRepository,
            userInteractor = userInteractor,
            metadataInteractor = metadataInteractor,
            strigaOtpConfirmInteractor = mockk(relaxed = true),
            strigaUserStatusRepository = mockk(relaxed = true)
        )
    }

    private fun initSignupDataRepository(savedData: List<StrigaSignupData>) {
        savedSignupDataStorage.clear()
        savedSignupDataStorage.addAll(savedData)

        coEvery {
            signupDataRepository.updateSignupData(any<StrigaSignupData>())
        } answers {
            savedSignupDataStorage.add(arg(0))
            StrigaDataLayerResult.Success(Unit)
        }
        coEvery {
            signupDataRepository.updateSignupData(any<Collection<StrigaSignupData>>())
        } answers {
            savedSignupDataStorage.addAll(arg(0))
            StrigaDataLayerResult.Success(Unit)
        }
        coEvery { signupDataRepository.getUserSignupData() } answers {
            StrigaDataLayerResult.Success(savedSignupDataStorage)
        }
    }

    private fun setData(type: StrigaSignupDataType, newValue: String) {
        firstStepData[type] = StrigaSignupData(type, newValue)
    }
}
