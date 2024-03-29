package org.p2p.wallet.jupiter.ui.main

import io.mockk.verify
import org.junit.ClassRule
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.utils.CoroutineExtension
import org.p2p.wallet.utils.SpyOnInjectMockKsExtension
import org.p2p.wallet.utils.TimberUnitTestInstance

@ExperimentalCoroutinesApi
@ExtendWith(SpyOnInjectMockKsExtension::class, CoroutineExtension::class)
class JupiterSwapPresenterBackPressTest : JupiterSwapPresenterBaseTest() {

    companion object {
        @ClassRule
        @JvmField
        val timber = TimberUnitTestInstance(
            isEnabled = false,
            defaultTag = "Swap:BackPress"
        )
    }

    @Test
    fun `GIVEN back button handler WHEN onBackPressed() THEN check closeScreen() IS CALLED`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        presenter.attach(view)
        presenter.onBackPressed()
        advanceUntilIdle()
        // keyboard should be shown
        verify(exactly = 1) { view.closeScreen() }

        presenter.detach()
    }
}
