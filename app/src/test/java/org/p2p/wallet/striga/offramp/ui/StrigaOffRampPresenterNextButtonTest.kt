package org.p2p.wallet.striga.offramp.ui

import io.mockk.verify
import org.junit.ClassRule
import org.junit.Test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.R
import org.p2p.wallet.striga.offramp.models.StrigaOffRampButtonState
import org.p2p.wallet.utils.StandardTestCoroutineDispatchers
import org.p2p.wallet.utils.TimberUnitTestInstance

@OptIn(ExperimentalCoroutinesApi::class)
class StrigaOffRampPresenterNextButtonTest : StrigaOffRampPresenterBaseTest() {

    companion object {
        @ClassRule
        @JvmField
        val timber = TimberUnitTestInstance(
            isEnabled = true,
            defaultTag = "StrigaOffRamp:NextButton"
        )
    }

    @Test
    fun `GIVEN striga off ramp WHEN no internet THEN check button is disabled with error`() = runTest {
        hasInternetState.value = false
        val presenter = createPresenter()

        presenter.attach(view)
        advanceUntilIdle()

        presenter.onSubmit()

        verify {
            view.showUiKitSnackBar(messageResId = R.string.error_no_internet_message)
            view.setButtonState(StrigaOffRampButtonState.ErrorGeneral)
        }
    }

    // todo: this test is not working yet because of coroutines synchronization in tests; needs to investigate
    fun `GIVE striga off ramp WHEN internet disconnected and reconnected THEN check button disabled and enabled`() =
        runTest {
            val presenter = createPresenter(StandardTestCoroutineDispatchers())
            hasInternetState.emit(false)

            presenter.attach(view)
            advanceUntilIdle()

            verify {
                // 1. loading
                view.setButtonState(StrigaOffRampButtonState.LoadingRates)
                // 2. no internet - error
                view.setButtonState(StrigaOffRampButtonState.ErrorGeneral)
            }

            hasInternetState.emit(true)
            advanceTimeUntilRatesHasCome()

            verify {
                // 3. internet appeared - loading
                view.setButtonState(StrigaOffRampButtonState.LoadingRates)
                // 4. rates loaded - show enter amount button
                view.setButtonState(StrigaOffRampButtonState.EnterAmount)
            }
        }
}
