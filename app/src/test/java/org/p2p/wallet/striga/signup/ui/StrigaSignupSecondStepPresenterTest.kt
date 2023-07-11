package org.p2p.wallet.striga.signup.ui

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.ClassRule
import org.junit.Test
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.core.common.di.AppScope
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.R
import org.p2p.wallet.alarmlogger.logger.AlarmErrorsLogger
import org.p2p.wallet.auth.interactor.MetadataInteractor
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.common.InAppFeatureFlags
import org.p2p.wallet.common.feature_toggles.toggles.inapp.StrigaSimulateWeb3Flag
import org.p2p.wallet.striga.model.StrigaDataLayerResult
import org.p2p.wallet.striga.onboarding.interactor.StrigaOnboardingInteractor
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataItem
import org.p2p.wallet.striga.presetpicker.mapper.StrigaItemCellMapper
import org.p2p.wallet.striga.signup.StrigaSignUpSecondStepContract
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
import org.p2p.wallet.striga.signup.model.StrigaOccupation
import org.p2p.wallet.striga.signup.model.StrigaSignupFieldState
import org.p2p.wallet.striga.signup.model.StrigaSourceOfFunds
import org.p2p.wallet.striga.signup.repository.StrigaSignupDataLocalRepository
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupData
import org.p2p.wallet.striga.signup.repository.model.StrigaSignupDataType
import org.p2p.wallet.striga.signup.validation.StrigaSignupDataValidator
import org.p2p.wallet.striga.user.interactor.StrigaUserInteractor
import org.p2p.wallet.utils.TestAppScope
import org.p2p.wallet.utils.TimberUnitTestInstance
import org.p2p.wallet.utils.UnconfinedTestDispatchers
import org.p2p.wallet.utils.back
import org.p2p.wallet.utils.mutableListQueueOf

private val SupportedCountry = CountryCode(
    countryName = "United Kingdom",
    flagEmoji = "🇬🇧",
    nameCodeAlpha2 = "gb",
    nameCodeAlpha3 = "gbr",
    phoneCode = "",
    mask = ""
)

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaSignupSecondStepPresenterTest {
    @MockK(relaxed = true)
    lateinit var countryCodeRepository: CountryCodeRepository

    @MockK(relaxed = true)
    lateinit var signupDataRepository: StrigaSignupDataLocalRepository

    @MockK
    lateinit var onboardingInteractor: StrigaOnboardingInteractor

    @MockK(relaxed = true)
    lateinit var userInteractor: StrigaUserInteractor

    @MockK(relaxed = true)
    lateinit var metadataInteractor: MetadataInteractor

    @MockK(relaxed = true)
    lateinit var inAppFeatureFlags: InAppFeatureFlags

    @MockK(relaxed = true)
    lateinit var alarmErrorsLogger: AlarmErrorsLogger

    lateinit var interactor: StrigaSignupInteractor

    private val signupDataValidator = StrigaSignupDataValidator()
    private val strigaItemCellMapper = StrigaItemCellMapper()

    private val dispatchers: CoroutineDispatchers = UnconfinedTestDispatchers()
    private val appScope: AppScope = TestAppScope(dispatchers.ui)

    companion object {
        @ClassRule
        @JvmField
        val timber = TimberUnitTestInstance("StrigaSignupSecondStepPresenterTest")
    }

    private fun createPresenter(): StrigaSignUpSecondStepPresenter {
        return StrigaSignUpSecondStepPresenter(
            dispatchers = dispatchers,
            interactor = interactor,
            onboardingInteractor = onboardingInteractor,
            strigaItemCellMapper = strigaItemCellMapper,
            alarmErrorsLogger = alarmErrorsLogger,
        )
    }

    @Before
    fun setUp() {
        MockKAnnotations.init(this)

        val simulateWeb3Flag = mockk<StrigaSimulateWeb3Flag>(relaxed = true) {
            every { featureValue } returns false
        }
        val simulateUserCreateFlag = mockk<StrigaSimulateWeb3Flag>(relaxed = true) {
            every { featureValue } returns false
        }
        every { inAppFeatureFlags.strigaSimulateWeb3Flag } returns simulateWeb3Flag
        every { inAppFeatureFlags.strigaSimulateWeb3Flag } returns simulateUserCreateFlag

        interactor = spyk(
            StrigaSignupInteractor(
                appScope = appScope,
                inAppFeatureFlags = inAppFeatureFlags,
                validator = signupDataValidator,
                countryCodeRepository = countryCodeRepository,
                signupDataRepository = signupDataRepository,
                userInteractor = userInteractor,
                metadataInteractor = metadataInteractor,
                strigaSmsInputInteractor = mockk(relaxed = true),
                strigaUserStatusRepository = mockk(relaxed = true)
            )
        )
    }

    @Test
    fun `GIVEN initial state WHEN presenter created THEN check presenter loads and sets saved data`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.COUNTRY_ALPHA_2, SupportedCountry.nameCodeAlpha2)
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha2(SupportedCountry.nameCodeAlpha2) } returns SupportedCountry

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
                SupportedCountry.nameCodeAlpha2
            ),
            resultSignupData[0]
        )
        assertEquals(
            StrigaSignupData(
                StrigaSignupDataType.COUNTRY_ALPHA_2,
                "${SupportedCountry.flagEmoji} ${SupportedCountry.countryName}"
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

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)

        presenter.onFieldChanged(StrigaSignupDataType.PHONE_NUMBER, "123")
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

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)

        presenter.onFieldChanged(StrigaSignupDataType.PHONE_NUMBER, "123")
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

        presenter.onFieldChanged(StrigaSignupDataType.CITY, "any city")
        presenter.onFieldChanged(StrigaSignupDataType.CITY_ADDRESS_LINE, "any address")
        presenter.onFieldChanged(StrigaSignupDataType.CITY_POSTAL_CODE, "any zip-code")
        presenter.onFieldChanged(StrigaSignupDataType.CITY_STATE, "any state")
        presenter.onPresetDataChanged(StrigaPresetDataItem.Occupation(StrigaOccupation("leafer", "some_emoji")))
        presenter.onPresetDataChanged(StrigaPresetDataItem.SourceOfFunds(StrigaSourceOfFunds("leafer")))
        presenter.onPresetDataChanged(StrigaPresetDataItem.Country(CountryCode("leafer", "emoji", "fr", "fra", "", "")))
        presenter.onSubmit()
        advanceUntilIdle()

        verify(exactly = 1) { view.setProgressIsVisible(true) }
        verify(exactly = 1) { view.navigateNext() }

        presenter.saveChanges()
        presenter.detach()
    }

    @Test
    fun `GIVEN error while creating user WHEN user clicks next THEN check snackback error`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "email@email.email")
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)

        coEvery { interactor.createUser() } throws Exception("error")

        presenter.onPresetDataChanged(StrigaPresetDataItem.Occupation(StrigaOccupation("loafer", "some_emoji")))
        presenter.onPresetDataChanged(StrigaPresetDataItem.SourceOfFunds(StrigaSourceOfFunds("unemployed")))
        presenter.onPresetDataChanged(StrigaPresetDataItem.Country(SupportedCountry))
        presenter.onFieldChanged(StrigaSignupDataType.CITY, "any city")
        presenter.onFieldChanged(StrigaSignupDataType.CITY_ADDRESS_LINE, "any address")
        presenter.onFieldChanged(StrigaSignupDataType.CITY_POSTAL_CODE, "any zip-code")
        presenter.onFieldChanged(StrigaSignupDataType.CITY_STATE, "any state")
        presenter.onSubmit()
        advanceUntilIdle()

        verify(exactly = 1) { view.setProgressIsVisible(true) }
        verify(exactly = 1) { view.showUiKitSnackBar("error", R.string.error_general_message) }
        verify(exactly = 1) { view.setProgressIsVisible(false) }
        verify(exactly = 0) { view.navigateNext() }

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
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha2(SupportedCountry.nameCodeAlpha2) } returns SupportedCountry

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onPresetDataChanged(StrigaPresetDataItem.SourceOfFunds(sourceOfFunds))
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
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha2(SupportedCountry.nameCodeAlpha2) } returns SupportedCountry

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onPresetDataChanged(StrigaPresetDataItem.Occupation(occupation))
        advanceUntilIdle()

        verify {
            view.updateSignupField(StrigaSignupDataType.OCCUPATION, occupation.occupationName)
            view.updateSignupField(StrigaSignupDataType.OCCUPATION, "Loafer")
        }
    }

    @Test
    fun `GIVEN invalid user input WHEN user inputs new data THEN check errors are cleared`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3, SupportedCountry.nameCodeAlpha3)
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha2(SupportedCountry.nameCodeAlpha2) } returns SupportedCountry
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha3(SupportedCountry.nameCodeAlpha3) } returns SupportedCountry

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onSubmit()
        presenter.onFieldChanged(StrigaSignupDataType.CITY, "123")
        advanceUntilIdle()

        verify { view.setErrors(any()) }
        verify { view.setButtonIsEnabled(any()) }
        verify { view.clearError(StrigaSignupDataType.CITY) }
    }

    @Test
    fun `GIVEN user input WHEN user types invalid data THEN check dynamic validation works after submit clicked`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.COUNTRY_OF_BIRTH_ALPHA_3, SupportedCountry.nameCodeAlpha3)
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha2(SupportedCountry.nameCodeAlpha2) } returns SupportedCountry
        coEvery { countryCodeRepository.findCountryCodeByIsoAlpha3(SupportedCountry.nameCodeAlpha3) } returns SupportedCountry

        val view = mockk<StrigaSignUpSecondStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onSubmit()
        presenter.onFieldChanged(StrigaSignupDataType.CITY, "")
        advanceUntilIdle()

        val errorsStates = mutableListQueueOf<List<StrigaSignupFieldState>>()

        verify { view.setErrors(capture(errorsStates)) }

        assertEquals(2, errorsStates.size)
        assertEquals(1, errorsStates[1].size)
        val firstNameErrorState = errorsStates.back()?.first()
        assertNotNull(firstNameErrorState)
        assertEquals(StrigaSignupDataType.CITY, firstNameErrorState.type)
        Assert.assertFalse(firstNameErrorState.isValid)
    }
}
