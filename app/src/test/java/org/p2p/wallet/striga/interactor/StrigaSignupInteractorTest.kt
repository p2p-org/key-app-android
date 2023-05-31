@file:OptIn(ExperimentalCoroutinesApi::class)

package org.p2p.wallet.striga.interactor

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.auth.repository.CountryRepository
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.model.StrigaDataLayerError
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.signup.validation.StrigaSignupDataValidator
import org.p2p.wallet.utils.TestAppScope
import org.p2p.wallet.utils.UnconfinedTestDispatchers

private val SupportedCountry = Country(
    name = "United Kingdom",
    flagEmoji = "🇬🇧",
    code = "gb"
)

private const val DefaultPhoneMask = "UA:380 ## ### ## ##"

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

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { countryRepository.findPhoneMaskByCountry(any()) } returns DefaultPhoneMask
        coEvery { countryRepository.detectCountryOrDefault() } returns SupportedCountry
    }

    @Test
    fun `GIVEN error WHEN getSignupData THEN check returned signup data list is empty`() = runTest {
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Failure(
            StrigaDataLayerError.InternalError(IllegalStateException("Known exception"))
        )

        val interactor = createInteractor()
        val result = interactor.getSignupData()
        assertEquals(0, result.size)
    }

    @Test
    fun `GIVEN signup data WHEN notify data changed THEN data is not updated without saveChanges`() = runTest {
        // GIVEN
        initSignupDataRepository(emptyList())
        val interactor = createInteractor()

        // WHEN
        interactor.notifyDataChanged(StrigaSignupDataType.PHONE_NUMBER, "1234567890")

        // THEN
        val result = interactor.getSignupData()
        assertEquals(0, result.size)
        assertEquals(0, savedSignupDataStorage.size)
    }

    @Test
    fun `GIVEN signup data WHEN notify data changed and saveChanges THEN data is updated`() = runTest {
        // GIVEN
        initSignupDataRepository(emptyList())
        val interactor = createInteractor()

        // WHEN
        interactor.notifyDataChanged(StrigaSignupDataType.PHONE_NUMBER, "1234567890")
        interactor.saveChanges()
        interactor.saveChanges()

        // THEN
        val result = interactor.getSignupData()
        assertEquals(1, result.size)
    }

    @Test
    fun `GIVEN invalid full signup data WHEN validate first step THEN validation result is invalid`() = runTest {
        // GIVEN
        initSignupDataRepository(emptyList())
        val interactor = createInteractor()

        // WHEN
        interactor.notifyDataChanged(StrigaSignupDataType.EMAIL, "1234567890")
        interactor.notifyDataChanged(StrigaSignupDataType.PHONE_NUMBER, "hello world")
        interactor.notifyDataChanged(StrigaSignupDataType.FIRST_NAME, "1234567890")
        interactor.notifyDataChanged(StrigaSignupDataType.LAST_NAME, "1234567890")
        interactor.notifyDataChanged(StrigaSignupDataType.DATE_OF_BIRTH, "1234567890")
        interactor.notifyDataChanged(StrigaSignupDataType.COUNTRY_OF_BIRTH, "1234567890")
        val (isValid, _) = interactor.validateFirstStep()
        val (isValidSecond, _) = interactor.validateSecondStep()

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
        interactor.notifyDataChanged(StrigaSignupDataType.EMAIL, "aaa@bbb.ccc")
        interactor.notifyDataChanged(StrigaSignupDataType.PHONE_NUMBER, "+1234567890")
        interactor.notifyDataChanged(StrigaSignupDataType.FIRST_NAME, "Vasya")
        interactor.notifyDataChanged(StrigaSignupDataType.LAST_NAME, "Pupkin")
        interactor.notifyDataChanged(StrigaSignupDataType.DATE_OF_BIRTH, "10.10.2010")
        interactor.notifyDataChanged(StrigaSignupDataType.COUNTRY_OF_BIRTH, "Somewhere")
        val (isValid, _) = interactor.validateFirstStep()
        val (isValidSecond, _) = interactor.validateSecondStep()

        // THEN
        assertTrue(isValid)
        assertFalse(isValidSecond)
    }

    @Test
    fun `GIVEN invalid full signup data WHEN validate second step THEN validation result is invalid`() = runTest {
        // GIVEN
        initSignupDataRepository(emptyList())
        val interactor = createInteractor()

        // WHEN
        interactor.notifyDataChanged(StrigaSignupDataType.OCCUPATION, "")
        interactor.notifyDataChanged(StrigaSignupDataType.SOURCE_OF_FUNDS, " world")
        interactor.notifyDataChanged(StrigaSignupDataType.COUNTRY, "")
        interactor.notifyDataChanged(StrigaSignupDataType.CITY, "")
        interactor.notifyDataChanged(StrigaSignupDataType.CITY_ADDRESS_LINE, "")
        interactor.notifyDataChanged(StrigaSignupDataType.CITY_POSTAL_CODE, "")
        interactor.notifyDataChanged(StrigaSignupDataType.CITY_STATE, "")
        val (isValid, _) = interactor.validateSecondStep()
        val (isValidFirst, _) = interactor.validateFirstStep()

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
        interactor.notifyDataChanged(StrigaSignupDataType.OCCUPATION, "a")
        interactor.notifyDataChanged(StrigaSignupDataType.SOURCE_OF_FUNDS, "a")
        interactor.notifyDataChanged(StrigaSignupDataType.COUNTRY, "a")
        interactor.notifyDataChanged(StrigaSignupDataType.CITY, "a")
        interactor.notifyDataChanged(StrigaSignupDataType.CITY_ADDRESS_LINE, "a")
        interactor.notifyDataChanged(StrigaSignupDataType.CITY_POSTAL_CODE, "a")
        interactor.notifyDataChanged(StrigaSignupDataType.CITY_STATE, "a")
        val (isValid, _) = interactor.validateSecondStep()
        val (isValidFirst, _) = interactor.validateFirstStep()

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
}
