package org.p2p.wallet.home.onofframp.ui

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.auth.model.CountryCode
import org.p2p.wallet.home.onofframp.OnOffRampCountrySelectionContract
import org.p2p.wallet.home.onofframp.interactor.OnOffRampCountrySelectionInteractor
import org.p2p.wallet.settings.interactor.SettingsInteractor
import org.p2p.wallet.utils.StandardTestCoroutineDispatchers
import org.p2p.wallet.utils.coVerifyNone
import org.p2p.wallet.utils.coVerifyOnce
import org.p2p.wallet.utils.verifyOnce

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
class OnOffRampCountrySelectionPresenterTest {

    @MockK
    lateinit var interactor: OnOffRampCountrySelectionInteractor

    @MockK
    lateinit var settingsInteractor: SettingsInteractor

    @MockK(relaxed = true)
    lateinit var view: OnOffRampCountrySelectionContract.View

    private val dispatchers: CoroutineDispatchers = StandardTestCoroutineDispatchers()

    private var userCountry: CountryCode? = null

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { settingsInteractor.userCountryCode } returns userCountry
        every { settingsInteractor.userCountryCode = any() } answers { userCountry = arg(0) }
        coEvery { interactor.saveCurrentCountry(any()) } answers { userCountry = arg(0) }
    }

    private fun createPresenter(): OnOffRampCountrySelectionContract.Presenter {
        return OnOffRampCountrySelectionPresenter(
            dispatchers = dispatchers,
            addMoneyOnboardingInteractor = interactor,
            settingsInteractor = settingsInteractor
        )
    }

    @Test
    fun `GIVEN initial state WHEN attach THEN show default detected country`() = runTest {
        coEvery { interactor.getChosenCountry() } returns SupportedCountry
        val presenter = createPresenter()

        presenter.attach(view)
        advanceUntilIdle()

        verifyOnce { view.setCurrentCountry(SupportedCountry) }
        assertNull(settingsInteractor.userCountryCode)

        presenter.detach()
    }

    @Test
    fun `GIVEN initial state WHEN clicked country THEN show country picker`() {
        coEvery { interactor.getChosenCountry() } returns SupportedCountry
        val presenter = createPresenter()

        presenter.attach(view)
        presenter.onCountryClicked()

        // null argument because when take the country only from settings, not from default-detected
        verifyOnce { view.showCountryPicker(null) }
        assertNull(settingsInteractor.userCountryCode)

        presenter.detach()
    }

    @Test
    fun `GIVEN initial state WHEN clicked next THEN navigate next and save country`(): Unit = runTest {
        coEvery { interactor.getChosenCountry() } returns SupportedCountry
        val presenter = createPresenter()

        presenter.attach(view)
        presenter.onNextClicked()
        advanceUntilIdle()

        coVerifyOnce { interactor.saveCurrentCountry(SupportedCountry) }
        verifyOnce { view.navigateNext() }

        presenter.detach()
    }

    @Test
    fun `GIVEN selected country WHEN country picker closed with result THEN check country is not saved to settings`() =
        runTest {
            coEvery { interactor.getChosenCountry() } returns SupportedCountry
            val presenter = createPresenter()

            presenter.attach(view)
            presenter.onCurrentCountryChanged(UnsupportedCountry)
            advanceUntilIdle()

            coVerifyNone { interactor.saveCurrentCountry(UnsupportedCountry) }

            presenter.detach()
        }
}
