package org.p2p.wallet.jupiter.ui.main

import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel
import org.p2p.wallet.utils.CoroutineExtension
import org.p2p.wallet.utils.SpyOnInjectMockKsExtension
import org.p2p.wallet.utils.front
import org.p2p.wallet.utils.mutableListQueueOf
import org.p2p.wallet.utils.pop

@ExperimentalCoroutinesApi
@ExtendWith(SpyOnInjectMockKsExtension::class, CoroutineExtension::class)
class JupiterSwapPresenterChangeTokensTest : JupiterSwapPresenterBaseTest() {

    @Test
    fun `GIVEN swap screen WHEN onChangeTokenAClick() THEN check openChangeTokenAScreen() IS CALLED`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        presenter.attach(view)
        presenter.onChangeTokenAClick()
        advanceUntilIdle()

        verify(exactly = 1) { view.openChangeTokenAScreen() }
        verify(exactly = 0) { view.openChangeTokenBScreen() }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN onChangeTokenBClick() THEN check openChangeTokenBScreen() IS CALLED`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        presenter.attach(view)
        presenter.onChangeTokenBClick()
        advanceUntilIdle()

        verify(exactly = 0) { view.openChangeTokenAScreen() }
        verify(exactly = 1) { view.openChangeTokenBScreen() }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen is not attached WHEN onChangeTokenAClick() THEN check openChangeTokenAScreen() IS NOT CALLED`() = runTest {
        val presenter = createPresenter {
            homeRepoAllTokens = mutableListOf()
            homeRepoUserTokens = homeRepoAllTokens
        }

        // idk how to reproduce it better, but at least it covers the case when changing isn't available
        presenter.onChangeTokenAClick()
        presenter.attach(view)
        advanceUntilIdle()

        verify(exactly = 0) { view.openChangeTokenAScreen() }
        verify(exactly = 0) { view.openChangeTokenBScreen() }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen is not attached WHEN onChangeTokenBClick() THEN check openChangeTokenBScreen() IS NOT CALLED`() = runTest {
        val presenter = createPresenter {
            homeRepoAllTokens = mutableListOf()
            homeRepoUserTokens = homeRepoAllTokens
        }

        // idk how to reproduce it better, but at least it covers the case when changing isn't available
        presenter.onChangeTokenBClick()
        presenter.attach(view)
        advanceUntilIdle()

        verify(exactly = 0) { view.openChangeTokenAScreen() }
        verify(exactly = 0) { view.openChangeTokenBScreen() }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN called switchTokens THEN check tokens are actually switched`() = runTest {
        val (firstToken, secondToken, presenter) = createPresenterAndTokens()
        presenter.attach(view)
        presenter.switchTokens()
        advanceUntilIdle()

        // collecting view calls
        val firstTokenStates = mutableListQueueOf<SwapWidgetModel>()
        val secondTokenStates = mutableListQueueOf<SwapWidgetModel>()

        // by 2 states each, because first is initial, second is switched
        verify(exactly = 2) { view.setFirstTokenWidgetState(capture(firstTokenStates)) }
        verify(exactly = 2) { view.setSecondTokenWidgetState(capture(secondTokenStates)) }

        assertTrue(firstTokenStates.isNotEmpty())
        assertTrue(secondTokenStates.isNotEmpty())

        // verify content of the first widget
        assertTrue(firstTokenStates.front() is SwapWidgetModel.Content)
        checkFirstSwapWidgetModel(firstToken, firstTokenStates.front(), "")

        // verify content of the second widget
        assertTrue(secondTokenStates.front() is SwapWidgetModel.Content)
        checkSecondSwapWidgetModel(secondToken, secondTokenStates.front())

        firstTokenStates.pop()
        secondTokenStates.pop()

        // now verify that tokens are actually switched
        // verify content of the first widget with secondToken
        assertTrue(firstTokenStates.front() is SwapWidgetModel.Content)
        checkFirstSwapWidgetModel(secondToken, firstTokenStates.front(), "")

        // verify content of the second widget with secondToken
        assertTrue(secondTokenStates.front() is SwapWidgetModel.Content)
        checkSecondSwapWidgetModel(firstToken, secondTokenStates.front())

        presenter.detach()
    }
}
