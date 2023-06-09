package org.p2p.wallet.striga.onboarding

import assertk.assertThat
import assertk.assertions.isEqualTo
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.auth.repository.CountryCodeRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.onboarding.interactor.StrigaOnboardingInteractor
import org.p2p.wallet.striga.signup.StrigaPresetDataLocalRepository
import org.p2p.wallet.utils.UnconfinedTestDispatchers
import org.p2p.wallet.utils.back
import org.p2p.wallet.utils.front
import org.p2p.wallet.utils.mutableListQueueOf

private val UnsupportedCountry = CountryCode(
    countryName = "Turkey",
    flagEmoji = "🇹🇷",
    nameCodeAlpha2 = "tr",
    nameCodeAlpha3 = "tur",
    phoneCode = "996",
    mask = "000"
)

private val SupportedCountry = CountryCode(
    countryName = "United Kingdom",
    flagEmoji = "🇬🇧",
    nameCodeAlpha2 = "gb",
    nameCodeAlpha3 = "gbr",
    phoneCode = "996",
    mask = "00"
)

@ExperimentalCoroutinesApi
class StrigaOnboardingPresenterTest {

    @MockK
    lateinit var countryRepository: CountryCodeRepository

    @MockK
    lateinit var strigaPresetDataLocalRepository: StrigaPresetDataLocalRepository

    @MockK
    lateinit var interactor: StrigaOnboardingInteractor

    private val dispatchers: CoroutineDispatchers = UnconfinedTestDispatchers()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createPresenter(): StrigaOnboardingContract.Presenter {
        return StrigaOnboardingPresenter(
            dispatchers = dispatchers,
            interactor = interactor
        )
    }

    @Test
    fun `GIVEN unsupported country WHEN presenter initialized THEN check button state is ChangeCountry`() = runTest {
        coEvery { interactor.getChosenCountry() } returns UnsupportedCountry
        coEvery { interactor.checkIsCountrySupported(any()) } answers { arg<CountryCode>(0) == SupportedCountry }
        coEvery { interactor.saveCurrentCountry(any()) }.returns(Unit)

        val view: StrigaOnboardingContract.View = mockk(relaxed = true)

        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        val countryStates = mutableListQueueOf<CountryCode>()
        val availabilityStates = mutableListQueueOf<StrigaOnboardingContract.View.AvailabilityState>()
        verify(exactly = 1) { view.setCurrentCountry(capture(countryStates)) }
        verify(exactly = 1) { view.setAvailabilityState(capture(availabilityStates)) }
        verify(exactly = 0) { view.navigateNext() }

        assertThat(UnsupportedCountry).isEqualTo(countryStates.back())
        assertThat(StrigaOnboardingContract.View.AvailabilityState.Unavailable).isEqualTo(availabilityStates.back())

        presenter.detach()
    }

    @Test
    fun `GIVEN supported country WHEN presenter initialized THEN check button state is Continue`() = runTest {
        coEvery { interactor.getChosenCountry() } returns SupportedCountry
        coEvery { interactor.checkIsCountrySupported(any()) } answers { arg<CountryCode>(0) == SupportedCountry }
        coEvery { interactor.saveCurrentCountry(any()) }.returns(Unit)

        val view: StrigaOnboardingContract.View = mockk(relaxed = true)

        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        val countryStates = mutableListQueueOf<CountryCode>()
        val availabilityStates = mutableListQueueOf<StrigaOnboardingContract.View.AvailabilityState>()
        verify(exactly = 1) { view.setCurrentCountry(capture(countryStates)) }
        verify(exactly = 1) { view.setAvailabilityState(capture(availabilityStates)) }
        verify(exactly = 0) { view.navigateNext() }

        assertEquals(SupportedCountry, countryStates.back())
        assertEquals(StrigaOnboardingContract.View.AvailabilityState.Available, availabilityStates.back())

        presenter.detach()
    }

    @Test
    fun `GIVEN unsupported country and changed to supported WHEN presenter initialized THEN check button state is Continue`() = runTest {
        coEvery { interactor.getChosenCountry() } returns UnsupportedCountry
        coEvery { interactor.checkIsCountrySupported(any()) } answers { arg<CountryCode>(0) == SupportedCountry }
        coEvery { interactor.saveCurrentCountry(any()) }.returns(Unit)

        val view: StrigaOnboardingContract.View = mockk(relaxed = true)

        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        // changing country
        presenter.onCurrentCountryChanged(SupportedCountry)

        val countryStates = mutableListQueueOf<CountryCode>()
        val availabilityStates = mutableListQueueOf<StrigaOnboardingContract.View.AvailabilityState>()
        verify(exactly = 2) { view.setCurrentCountry(capture(countryStates)) }
        verify(exactly = 2) { view.setAvailabilityState(capture(availabilityStates)) }
        verify(exactly = 0) { view.navigateNext() }

        assertEquals(UnsupportedCountry, countryStates.front())
        assertEquals(SupportedCountry, countryStates.back())
        assertEquals(StrigaOnboardingContract.View.AvailabilityState.Unavailable, availabilityStates.front())
        assertEquals(StrigaOnboardingContract.View.AvailabilityState.Available, availabilityStates.back())

        presenter.detach()
    }

    @Test
    fun `GIVEN initial state WHEN selected unsupported country THEN check button state is ChangeCountry`() = runTest {
        coEvery { interactor.getChosenCountry() } returns UnsupportedCountry
        coEvery { interactor.checkIsCountrySupported(any()) } answers { arg<CountryCode>(0) == SupportedCountry }
        coEvery { interactor.saveCurrentCountry(any()) }.returns(Unit)

        val view: StrigaOnboardingContract.View = mockk(relaxed = true)

        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        // changing country
        presenter.onCurrentCountryChanged(UnsupportedCountry)

        val countryStates = mutableListQueueOf<CountryCode>()
        val availabilityStates = mutableListQueueOf<StrigaOnboardingContract.View.AvailabilityState>()
        verify(exactly = 2) { view.setCurrentCountry(capture(countryStates)) }
        verify(exactly = 2) { view.setAvailabilityState(capture(availabilityStates)) }
        verify(exactly = 0) { view.navigateNext() }

        assertEquals(UnsupportedCountry, countryStates.back())
        assertEquals(StrigaOnboardingContract.View.AvailabilityState.Unavailable, availabilityStates.back())

        presenter.detach()
    }

    @Test
    fun `GIVEN supported country WHEN clicked continue THEN check navigate to next destination`() = runTest {
        coEvery { countryRepository.detectCountryOrDefault() } returns SupportedCountry
        coEvery { strigaPresetDataLocalRepository.checkIsCountrySupported(any()) } answers {
            arg<CountryCode>(0) == SupportedCountry
        }

        val view: StrigaOnboardingContract.View = mockk(relaxed = true)

        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onClickContinue()
        advanceUntilIdle()

        verify(exactly = 1) { view.navigateNext() }

        presenter.detach()
    }

    @Test
    fun `GIVEN initial state WHEN clicked help text THEN check navigate to help`() = runTest {
        coEvery { countryRepository.detectCountryOrDefault() } returns SupportedCountry
        coEvery { strigaPresetDataLocalRepository.checkIsCountrySupported(any()) } answers {
            arg<CountryCode>(0) == SupportedCountry
        }
        val view: StrigaOnboardingContract.View = mockk(relaxed = true)

        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onClickHelp()
        advanceUntilIdle()

        verify(exactly = 1) { view.openHelp() }

        presenter.detach()
    }
}
