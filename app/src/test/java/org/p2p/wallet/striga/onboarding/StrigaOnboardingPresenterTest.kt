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
import org.p2p.wallet.auth.repository.Country
import org.p2p.wallet.auth.repository.CountryRepository
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.striga.onboarding.interactor.StrigaOnboardingInteractor
import org.p2p.wallet.striga.signup.StrigaPresetDataLocalRepository
import org.p2p.wallet.utils.UnconfinedTestDispatchers
import org.p2p.wallet.utils.back
import org.p2p.wallet.utils.front
import org.p2p.wallet.utils.mutableListQueueOf

private val UnsupportedCountry = Country(
    name = "Turkey",
    flagEmoji = "🇹🇷",
    codeAlpha2 = "tr",
    codeAlpha3 = "tur"
)

private val SupportedCountry = Country(
    name = "United Kingdom",
    flagEmoji = "🇬🇧",
    codeAlpha2 = "gb",
    codeAlpha3 = "gbr"
)

@ExperimentalCoroutinesApi
class StrigaOnboardingPresenterTest {

    @MockK
    lateinit var countryRepository: CountryRepository

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
        coEvery { interactor.checkIsCountrySupported(any()) } answers { arg<Country>(0) == SupportedCountry }

        val view: StrigaOnboardingContract.View = mockk(relaxed = true)

        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        val countryStates = mutableListQueueOf<Country>()
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
        coEvery { interactor.checkIsCountrySupported(any()) } answers { arg<Country>(0) == SupportedCountry }

        val view: StrigaOnboardingContract.View = mockk(relaxed = true)

        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        val countryStates = mutableListQueueOf<Country>()
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
        coEvery { interactor.checkIsCountrySupported(any()) } answers { arg<Country>(0) == SupportedCountry }

        val view: StrigaOnboardingContract.View = mockk(relaxed = true)

        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        // changing country
        coEvery { interactor.getChosenCountry() } returns SupportedCountry
        presenter.attach(view)
        advanceUntilIdle()

        val countryStates = mutableListQueueOf<Country>()
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
        coEvery { interactor.checkIsCountrySupported(any()) } answers { arg<Country>(0) == SupportedCountry }

        val view: StrigaOnboardingContract.View = mockk(relaxed = true)

        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        // changing country
        coEvery { interactor.getChosenCountry() } returns UnsupportedCountry
        presenter.attach(view)
        advanceUntilIdle()

        val countryStates = mutableListQueueOf<Country>()
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
            arg<Country>(0) == SupportedCountry
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
            arg<Country>(0) == SupportedCountry
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
