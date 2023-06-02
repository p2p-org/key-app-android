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
import org.p2p.wallet.striga.signup.StrigaSignUpFirstStepContract
import org.p2p.wallet.striga.signup.interactor.StrigaSignupInteractor
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
class StrigaSignupFirstStepPresenterTest {

    @MockK(relaxed = true)
    lateinit var countryRepository: CountryRepository

    @MockK(relaxed = true)
    lateinit var signupDataRepository: StrigaSignupDataLocalRepository

    lateinit var interactor: StrigaSignupInteractor

    private val signupDataValidator = StrigaSignupDataValidator()

    private val dispatchers: CoroutineDispatchers = UnconfinedTestDispatchers()
    private val appScope: AppScope = TestAppScope(dispatchers.ui)

    init {
        plantTimberToStdout("StrigaSignupFirstStepPresenterTest")
    }

    private fun createPresenter(): StrigaSignUpFirstStepPresenter {
        return StrigaSignUpFirstStepPresenter(
            dispatchers = dispatchers,
            interactor = interactor,
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
            StrigaSignupData(StrigaSignupDataType.EMAIL, "email@email.email")
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryRepository.findPhoneMaskByCountry(any()) } returns DefaultPhoneMask

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        val updatedFieldValueStates = mutableListQueueOf<String>()
        val updatedFieldTypeStates = mutableListQueueOf<StrigaSignupDataType>()
        verify(exactly = initialSignupData.size) {
            view.updateSignupField(
                capture(updatedFieldTypeStates),
                capture(updatedFieldValueStates)
            )
        }
        verify(exactly = 1) { view.setPhoneMask(any()) }
        verify(exactly = 0) { view.setErrors(any()) }
        verify(exactly = 0) { view.clearErrors() }
        verify(exactly = 0) { view.setButtonIsEnabled(any()) }
        verify(exactly = 0) { view.navigateNext() }

        val resultSignupData = updatedFieldTypeStates.mapIndexed { index, strigaSignupDataType ->
            StrigaSignupData(strigaSignupDataType, updatedFieldValueStates[index])
        }

        assertEquals(initialSignupData, resultSignupData)

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

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
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

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
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
        coEvery { countryRepository.findPhoneMaskByCountry(any()) } returns DefaultPhoneMask

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)

        presenter.onFieldChanged("+90 555 666 77 88", StrigaSignupDataType.PHONE_NUMBER)
        presenter.onFieldChanged("+90", StrigaSignupDataType.PHONE_CODE)
        presenter.onFieldChanged("Vasya", StrigaSignupDataType.FIRST_NAME)
        presenter.onFieldChanged("Pupkin", StrigaSignupDataType.LAST_NAME)
        presenter.onFieldChanged("10.10.2010", StrigaSignupDataType.DATE_OF_BIRTH)
        presenter.onFieldChanged("Uryupinks", StrigaSignupDataType.COUNTRY_OF_BIRTH)
        presenter.onSubmit()
        advanceUntilIdle()

        verify(exactly = 1) { view.navigateNext() }

        presenter.saveChanges()
        presenter.detach()
    }

    @Test
    fun `GIVEN initial state WHEN open country clicked THEN check country picker is opened`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "email@email.email")
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onCountryClicked()
        advanceUntilIdle()

        verify(exactly = 1) { view.showCountryPicker(any()) }
    }

    @Test
    fun `GIVEN selected country WHEN country chosen THEN check view updates country`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.EMAIL, "email@email.email")
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)

        val chosenCountry = Country("Turkey", "\uD83C\uDDF9\uD83C\uDDF7", "TR", "TUR")

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onCountryChanged(chosenCountry)
        advanceUntilIdle()

        verify(exactly = 1) {
            view.updateSignupField(
                StrigaSignupDataType.COUNTRY_OF_BIRTH,
                "${chosenCountry.flagEmoji} ${chosenCountry.name}"
            )
        }
    }

    @Test
    fun `GIVEN initial state with saved data WHEN presenter created THEN check country shows to user`() = runTest {
        val initialSignupData = listOf(
            StrigaSignupData(StrigaSignupDataType.COUNTRY_OF_BIRTH, SupportedCountry.codeAlpha3)
        )
        coEvery { signupDataRepository.getUserSignupData() } returns StrigaDataLayerResult.Success(initialSignupData)
        coEvery { countryRepository.findCountryByIsoAlpha2(SupportedCountry.codeAlpha2) } returns SupportedCountry
        coEvery { countryRepository.findCountryByIsoAlpha3(SupportedCountry.codeAlpha3) } returns SupportedCountry

        val view = mockk<StrigaSignUpFirstStepContract.View>(relaxed = true)
        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        verify {
            view.updateSignupField(
                StrigaSignupDataType.COUNTRY_OF_BIRTH,
                SupportedCountry.codeAlpha3
            )
            view.updateSignupField(
                StrigaSignupDataType.COUNTRY_OF_BIRTH,
                "${SupportedCountry.flagEmoji} ${SupportedCountry.name}"
            )
        }
    }
}
