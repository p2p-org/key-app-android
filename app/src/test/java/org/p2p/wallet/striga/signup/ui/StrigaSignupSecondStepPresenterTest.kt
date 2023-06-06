package org.p2p.wallet.striga.signup.ui

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.auth.model.PhoneMask
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.auth.repository.CountryRepository
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.onboarding.interactor.StrigaOnboardingInteractor
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.striga.presetpicker.mapper.StrigaItemCellMapper
import org.p2p.wallet.striga.signup.StrigaSignUpSecondStepContract
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.signup.validation.StrigaSignupDataValidator
import org.p2p.wallet.utils.TestAppScope
import org.p2p.wallet.utils.UnconfinedTestDispatchers
import org.p2p.wallet.utils.mutableListQueueOf
import org.p2p.wallet.utils.plantTimberToStdout

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
class StrigaSignupSecondStepPresenterTest {
    @MockK(relaxed = true)
    lateinit var countryRepository: CountryRepository

    @MockK(relaxed = true)
    lateinit var signupDataRepository: StrigaSignupDataLocalRepository

    @MockK
    lateinit var onboardingInteractor: StrigaOnboardingInteractor

    lateinit var interactor: StrigaSignupInteractor

    private val signupDataValidator = StrigaSignupDataValidator()
    private val strigaItemCellMapper = StrigaItemCellMapper()

    private val dispatchers: CoroutineDispatchers = UnconfinedTestDispatchers()
    private val appScope: AppScope = TestAppScope(dispatchers.ui)

    init {
        plantTimberToStdout("StrigaSignupSecondStepPresenterTest")
    }

    private fun createPresenter(): StrigaSignUpSecondStepPresenter {
        return StrigaSignUpSecondStepPresenter(
            dispatchers = dispatchers,
            interactor = interactor,
            onboardingInteractor = onboardingInteractor,
            strigaItemCellMapper = strigaItemCellMapper,
        )
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        interactor = StrigaSignupInteractor(
            appScope = appScope,
            validator = signupDataValidator,
            countryRepository = countryRepository,
            signupDataRepository = signupDataRepository,
        )
    }

    @Test
    fun `GIVEN initial state WHEN presenter created THEN check presenter loads and sets saved data`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.COUNTRY_ALPHA_2, SupportedCountry.codeAlpha2)
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryRepository.findCountryByIsoAlpha2(SupportedCountry.codeAlpha2) } returns SupportedCountry

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        val updatedFieldValueStates = mutableListQueueOf<String>()
        val updatedFieldTypeStates = mutableListQueueOf<StrigaSignupDataType>()
        verify(exactly = 2) {
            view.updateSignupField(
                capture(updatedFieldTypeStates),
                capture(updatedFieldValueStates)
            )
        }
        verify(exactly = 0) { view.setErrors(any()) }
        verify(exactly = 0) { view.clearErrors() }
        verify(exactly = 0) { view.setButtonIsEnabled(any()) }
        verify(exactly = 0) { view.navigateNext() }

        val resultSignupData = updatedFieldTypeStates.mapIndexed { index, strigaSignupDataType ->
            StrigaSignupData(strigaSignupDataType, updatedFieldValueStates[index])
        }

        assertEquals(2, resultSignupData.size)
        assertEquals(
            StrigaSignupData(
                StrigaSignupDataType.COUNTRY_ALPHA_2,
                SupportedCountry.codeAlpha2
            ),
            resultSignupData[0]
        )
        assertEquals(
            StrigaSignupData(
                StrigaSignupDataType.COUNTRY_ALPHA_2,
                "${SupportedCountry.flagEmoji} ${SupportedCountry.name}"
            ),
            resultSignupData[1]
        )

        presenter.saveChanges()
        presenter.detach()
    }

    /**
     * Don't show errors until pressed next button
     */
    @Test
    fun `GIVEN invalid user data WHEN input changed with wrong value THEN check nothing happens`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "email@email.email")
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryRepository.findPhoneMaskByCountry(any()) } returns DefaultPhoneMask

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)

        presenter.onFieldChanged("123", StrigaSignupDataType.PHONE_NUMBER)
        advanceUntilIdle()

        verify(exactly = 0) { view.setErrors(any()) }
        verify(exactly = 0) { view.setButtonIsEnabled(false) }

        presenter.saveChanges()
        presenter.detach()
    }

    @Test
    fun `GIVEN invalid user data WHEN next clicked THEN check errors are shown`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "email@email.email")
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryRepository.findPhoneMaskByCountry(any()) } returns DefaultPhoneMask

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)

        presenter.onFieldChanged("123", StrigaSignupDataType.PHONE_NUMBER)
        presenter.onSubmit()
        advanceUntilIdle()

        verify(exactly = 1) { view.setErrors(any()) }
        verify(exactly = 1) { view.setButtonIsEnabled(false) }
        verify(exactly = 1) { view.scrollToFirstError(any()) }

        presenter.saveChanges()
        presenter.detach()
    }

    @Test
    fun `GIVEN valid user data WHEN next clicked THEN check we go to next screen`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "email@email.email")
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)

        presenter.onFieldChanged("any occupation", StrigaSignupDataType.OCCUPATION)
        presenter.onFieldChanged("any source", StrigaSignupDataType.SOURCE_OF_FUNDS)
        presenter.onFieldChanged("any country", StrigaSignupDataType.COUNTRY_ALPHA_2)
        presenter.onFieldChanged("any city", StrigaSignupDataType.CITY)
        presenter.onFieldChanged("any address", StrigaSignupDataType.CITY_ADDRESS_LINE)
        presenter.onFieldChanged("any zip-code", StrigaSignupDataType.CITY_POSTAL_CODE)
        presenter.onFieldChanged("any state", StrigaSignupDataType.CITY_STATE)
        presenter.onSubmit()
        advanceUntilIdle()

        verify(exactly = 1) { view.navigateNext() }

        presenter.saveChanges()
        presenter.detach()
    }

    @Test
    fun `GIVEN initial state with saved data WHEN presenter created THEN check sources of funds shows to user`() = runTest {
        val sourceOfFunds = StrigaSourceOfFunds("PERSONAL_SAVINGS")
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.SOURCE_OF_FUNDS, sourceOfFunds.sourceName)
        )
        coEvery { onboardingInteractor.getSourcesOfFundsByName(sourceOfFunds.sourceName) } returns sourceOfFunds
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryRepository.findCountryByIsoAlpha2(SupportedCountry.codeAlpha2) } returns SupportedCountry

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onPresetDataChanged(StrigaPresetDataItem.StrigaSourceOfFundsItem(sourceOfFunds))
        advanceUntilIdle()

        verify {
            view.updateSignupField(StrigaSignupDataType.SOURCE_OF_FUNDS, sourceOfFunds.sourceName)
            view.updateSignupField(StrigaSignupDataType.SOURCE_OF_FUNDS, "Personal savings")
        }
    }

    @Test
    fun `GIVEN initial state with saved data WHEN presenter created THEN check occupation shows to user`() = runTest {
        val occupation = StrigaOccupation("Loafer", "\uD83C\uDFA8")
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.OCCUPATION, occupation.occupationName)
        )
        coEvery { onboardingInteractor.getOccupationByName(occupation.occupationName) } returns occupation
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryRepository.findCountryByIsoAlpha2(SupportedCountry.codeAlpha2) } returns SupportedCountry

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onPresetDataChanged(StrigaPresetDataItem.StrigaOccupationItem(occupation))
        advanceUntilIdle()

        verify {
            view.updateSignupField(StrigaSignupDataType.OCCUPATION, occupation.occupationName)
            view.updateSignupField(StrigaSignupDataType.OCCUPATION, "Loafer")
        }
    }

    @Test
    fun `GIVEN invalid user input WHEN user inputs new data THEN check errors are cleared`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3, SupportedCountry.codeAlpha3)
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryRepository.findCountryByIsoAlpha2(SupportedCountry.codeAlpha2) } returns SupportedCountry
        coEvery { countryRepository.findCountryByIsoAlpha3(SupportedCountry.codeAlpha3) } returns SupportedCountry

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onSubmit()
        presenter.onFieldChanged("123", StrigaSignupDataType.CITY)
        advanceUntilIdle()

        verify { view.setErrors(any()) }
        verify { view.setButtonIsEnabled(any()) }
        verify { view.clearError(StrigaSignupDataType.CITY) }
    }
}
