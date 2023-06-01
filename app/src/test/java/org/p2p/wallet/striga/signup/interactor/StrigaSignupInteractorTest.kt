@file:OptIn(ExperimentalCoroutinesApi::class)

package org.p2p.wallet.striga.signup.interactor

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.BeforeEach
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.auth.model.PhoneMask
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.auth.repository.CountryRepository
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.signup.validation.StrigaSignupDataValidator
import org.p2p.wallet.utils.TestAppScope
import org.p2p.wallet.utils.UnconfinedTestDispatchers

private val SupportedCountry = Country(
    name = "United Kingdom",
    flagEmoji = "🇬🇧",
    codeAlpha2 = "gb",
    codeAlpha3 = "gbr"
)

private val DefaultPhoneMask = PhoneMask(
    countryCodeAlpha2 = "ua",
    phoneCode = "+380",
    mask = "380 ## ### ## ##"
)

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaSignupInteractorTest {

    @MockK
    lateinit var signupDataRepository: StrigaSignupDataLocalRepository

    @MockK
    lateinit var countryRepository: CountryRepository

    private val signupDataValidator = StrigaSignupDataValidator()

    private val dispatchers: CoroutineDispatchers = UnconfinedTestDispatchers()
    private val appScope: AppScope = TestAppScope(dispatchers.ui)

    private val savedSignupDataStorage = mutableListOf<StrigaSignupData>()

    private var firstStepData = mutableMapOf<StrigaSignupDataType, StrigaSignupData>()
    private var secondStepData = mutableMapOf<StrigaSignupDataType, StrigaSignupData>()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { countryRepository.findPhoneMaskByCountry(any()) } returns DefaultPhoneMask
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
            StrigaSignupDataType.COUNTRY to StrigaSignupData(StrigaSignupDataType.COUNTRY, "TR")
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
        setData(StrigaSignupDataType.COUNTRY_OF_BIRTH, "1234567890")
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
        setData(StrigaSignupDataType.PHONE_CODE, "+1")
        setData(StrigaSignupDataType.PHONE_NUMBER, "+1234567890")
        setData(StrigaSignupDataType.FIRST_NAME, "Vasya")
        setData(StrigaSignupDataType.LAST_NAME, "Pupkin")
        setData(StrigaSignupDataType.DATE_OF_BIRTH, "10.10.2010")
        setData(StrigaSignupDataType.COUNTRY_OF_BIRTH, "Somewhere")
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
        setData(StrigaSignupDataType.COUNTRY, "")
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
        setData(StrigaSignupDataType.COUNTRY, "a")
        setData(StrigaSignupDataType.CITY, "a")
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
    fun `GIVEN existing phone mask for country WHEN findPhoneMaskByCountry THEN mask is not null`() = runTest {
        val interactor = createInteractor()
        val mask = interactor.findPhoneMaskByCountry(SupportedCountry)
        assertEquals(DefaultPhoneMask, mask)
    }

    @Test
    fun `GIVEN some country WHEN getSelectedCountry THEN check returned value is mock`() = runTest {
        val interactor = createInteractor()
        val selectedCountry = interactor.getSelectedCountry()
        assertEquals(selectedCountry, SupportedCountry)
    }

    private fun createInteractor(): StrigaSignupInteractor {
        return StrigaSignupInteractor(
            appScope = appScope,
            validator = signupDataValidator,
            countryRepository = countryRepository,
            signupDataRepository = signupDataRepository,
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
