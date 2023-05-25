package org.p2p.wallet.striga.onboarding

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
import org.p2p.wallet.striga.repository.StrigaPresetDataLocalRepository
import org.p2p.wallet.utils.UnconfinedTestDispatchers
import org.p2p.wallet.utils.back
import org.p2p.wallet.utils.front
import org.p2p.wallet.utils.mutableListQueueOf

private val UnsupportedCountry = Country(
    name = "Turkey",
    flagEmoji = "🇹🇷",
    code = "tr"
)

private val SupportedCountry = Country(
    name = "United Kingdom",
    flagEmoji = "🇬🇧",
    code = "gb"
)

@ExperimentalCoroutinesApi
class StrigaOnboardingPresenterTest {

    @MockK
    lateinit var countryRepository: CountryRepository

    @MockK
    lateinit var strigaPresetDataLocalRepository: StrigaPresetDataLocalRepository

    private val dispatchers: CoroutineDispatchers = UnconfinedTestDispatchers()

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    private fun createPresenter(): StrigaOnboardingContract.Presenter {
        return StrigaOnboardingPresenter(
            dispatchers = dispatchers,
            interactor = StrigaOnboardingInteractor(
                countryRepository = countryRepository,
                strigaPresetDataLocalRepository = strigaPresetDataLocalRepository
            )
        )
    }

    @Test
    fun `GIVEN unsupported country WHEN presenter initialized THEN check button state is ChangeCountry`() = runTest {
        coEvery { countryRepository.detectCountryOrDefault() } returns UnsupportedCountry
        coEvery { strigaPresetDataLocalRepository.checkIsCountrySupported(any()) } answers {
            arg<Country>(0) == SupportedCountry
        }

        val view: StrigaOnboardingContract.View = mockk(relaxed = true)

        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        val countryStates = mutableListQueueOf<Country>()
        val availabilityStates = mutableListQueueOf<StrigaOnboardingContract.View.AvailabilityState>()
        verify(exactly = 1) { view.setCurrentCountry(capture(countryStates)) }
        verify(exactly = 1) { view.setAvailabilityState(capture(availabilityStates)) }
        verify(exactly = 0) { view.openCountrySelection() }
        verify(exactly = 0) { view.navigateNext() }

        assertEquals(UnsupportedCountry, countryStates.back())
        assertEquals(StrigaOnboardingContract.View.AvailabilityState.Unavailable, availabilityStates.back())

        presenter.detach()
    }

    @Test
    fun `GIVEN supported country WHEN presenter initialized THEN check button state is Continue`() = runTest {
        coEvery { countryRepository.detectCountryOrDefault() } returns SupportedCountry
        coEvery { strigaPresetDataLocalRepository.checkIsCountrySupported(any()) } answers {
            arg<Country>(0) == SupportedCountry
        }

        val view: StrigaOnboardingContract.View = mockk(relaxed = true)

        val presenter = createPresenter()
        presenter.attach(view)
        advanceUntilIdle()

        val countryStates = mutableListQueueOf<Country>()
        val availabilityStates = mutableListQueueOf<StrigaOnboardingContract.View.AvailabilityState>()
        verify(exactly = 1) { view.setCurrentCountry(capture(countryStates)) }
        verify(exactly = 1) { view.setAvailabilityState(capture(availabilityStates)) }
        verify(exactly = 0) { view.openCountrySelection() }
        verify(exactly = 0) { view.navigateNext() }

        assertEquals(SupportedCountry, countryStates.back())
        assertEquals(StrigaOnboardingContract.View.AvailabilityState.Available, availabilityStates.back())

        presenter.detach()
    }

    @Test
    fun `GIVEN unsupported country and changed to supported WHEN presenter initialized THEN check button state is Continue`() = runTest {
        coEvery { countryRepository.detectCountryOrDefault() } returns UnsupportedCountry
        coEvery { strigaPresetDataLocalRepository.checkIsCountrySupported(any()) } answers {
            arg<Country>(0) == SupportedCountry
        }

        val view: StrigaOnboardingContract.View = mockk(relaxed = true)

        val presenter = createPresenter()
        presenter.attach(view)

        // changing country
        presenter.onCountrySelected(SupportedCountry)
        advanceUntilIdle()

        val countryStates = mutableListQueueOf<Country>()
        val availabilityStates = mutableListQueueOf<StrigaOnboardingContract.View.AvailabilityState>()
        verify(exactly = 2) { view.setCurrentCountry(capture(countryStates)) }
        verify(exactly = 2) { view.setAvailabilityState(capture(availabilityStates)) }
        verify(exactly = 0) { view.openCountrySelection() }
        verify(exactly = 0) { view.navigateNext() }

        assertEquals(UnsupportedCountry, countryStates.front())
        assertEquals(SupportedCountry, countryStates.back())
        assertEquals(StrigaOnboardingContract.View.AvailabilityState.Unavailable, availabilityStates.front())
        assertEquals(StrigaOnboardingContract.View.AvailabilityState.Available, availabilityStates.back())

        presenter.detach()
    }

    @Test
    fun `GIVEN initial state WHEN selected unsupported country THEN check button state is ChangeCountry`() = runTest {
        coEvery { countryRepository.detectCountryOrDefault() } returns UnsupportedCountry
        coEvery { strigaPresetDataLocalRepository.checkIsCountrySupported(any()) } answers {
            arg<Country>(0) == SupportedCountry
        }

        val view: StrigaOnboardingContract.View = mockk(relaxed = true)

        val presenter = createPresenter()
        presenter.attach(view)

        // changing country
        presenter.onCountrySelected(UnsupportedCountry)
        advanceUntilIdle()

        val countryStates = mutableListQueueOf<Country>()
        val availabilityStates = mutableListQueueOf<StrigaOnboardingContract.View.AvailabilityState>()
        verify(exactly = 2) { view.setCurrentCountry(capture(countryStates)) }
        verify(exactly = 2) { view.setAvailabilityState(capture(availabilityStates)) }
        verify(exactly = 0) { view.openCountrySelection() }
        verify(exactly = 0) { view.navigateNext() }

        assertEquals(UnsupportedCountry, countryStates.back())
        assertEquals(StrigaOnboardingContract.View.AvailabilityState.Unavailable, availabilityStates.back())

        presenter.detach()
    }

    @Test
    fun `GIVEN initial state WHEN clicked country THEN check country selection is opened`() = runTest {
        coEvery { countryRepository.detectCountryOrDefault() } returns SupportedCountry
        coEvery { strigaPresetDataLocalRepository.checkIsCountrySupported(any()) } answers {
            arg<Country>(0) == SupportedCountry
        }

        val view: StrigaOnboardingContract.View = mockk(relaxed = true)

        val presenter = createPresenter()
        presenter.attach(view)
        presenter.onClickChangeCountry()
        advanceUntilIdle()

        verify(exactly = 1) { view.openCountrySelection() }

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
