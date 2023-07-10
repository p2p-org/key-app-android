package org.p2p.wallet.jupiter.ui.main

import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.ClassRule
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.formatFiat
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.statemanager.price_impact.SwapPriceImpactView
import org.p2p.wallet.jupiter.ui.main.JupiterSwapTestHelpers.attachCallsLog
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel
import org.p2p.wallet.utils.CoroutineExtension
import org.p2p.wallet.utils.SpyOnInjectMockKsExtension
import org.p2p.wallet.utils.TimberUnitTestInstance
import org.p2p.wallet.utils.back
import org.p2p.wallet.utils.mutableListQueueOf

@ExperimentalCoroutinesApi
@ExtendWith(SpyOnInjectMockKsExtension::class, CoroutineExtension::class)
class JupiterSwapPresenterInitialStateTest : JupiterSwapPresenterBaseTest() {

    companion object {
        @ClassRule
        @JvmField
        val timber = TimberUnitTestInstance(defaultTag = "Swap:InitialState")
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check setRatioState values`() = runTest {
        val (firstToken, secondToken, presenter) = createPresenterAndTokens()
        // we do only attaching to view
        presenter.attach(view)
        advanceUntilIdle()

        val ratioStates = mutableListQueueOf<TextViewCellModel?>()

        verify { view.setRatioState(captureNullable(ratioStates)) }

        // check rates SOL -> USDC
        assertTrue(ratioStates.isNotEmpty())
        assertTrue(ratioStates.back() is TextViewCellModel.Raw)
        with(ratioStates.back() as TextViewCellModel.Raw) {
            val (tokenA, tokenB) = JupiterSwapTestHelpers.getTokensInNonFiatToFiatOrder(firstToken, secondToken)
            val container = text as TextContainer.Raw
            // we pre-defined rates at the beginning of the test [createPresenterAndTokens()]
            // 1 to 24.74
            // so it should be: 1 SOL ≈ 20.74 USDC
            assertEquals(
                JupiterSwapTestHelpers.formatRateString(tokenA.tokenSymbol, tokenA.rate!!.formatFiat(), tokenB.tokenSymbol),
                container.text
            )
        }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check setButtonState values`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        // we do only attaching to view
        presenter.attach(view)
        advanceUntilIdle()

        val buttonStates = mutableListQueueOf<SwapButtonState>()

        verify { view.setButtonState(capture(buttonStates)) }

        assertTrue(buttonStates.isNotEmpty())

        // why hide is initial state?
        // todo: it seems presenter never receives InitialLoading as it subscribes to state after it was emitted new value
        // todo: possible solutions: ignore, or SwapStateManger.state should be a SharedFlow if such behavior was intended
        // assertEquals(SwapButtonState.Hide, buttonStates.back())

        // check buttons is disabled as amount hasn't set
        assertTrue(buttonStates.isNotEmpty())
        assertTrue(buttonStates.back() is SwapButtonState.Disabled)
        with(buttonStates.back() as SwapButtonState.Disabled) {
            assertEquals(R.string.swap_main_button_enter_amount, (text as TextContainer.Res).textRes)
        }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check showKeyboard IS CALLED`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        // we do only attaching to view
        presenter.attach(view)
        advanceUntilIdle()
        // keyboard should be shown
        verify(exactly = 1) { view.showKeyboard() }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check showPriceImpact IS NOT CALLED`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        // we do only attaching to view
        presenter.attach(view)
        advanceUntilIdle()
        val priceImpactStates = mutableListQueueOf<SwapPriceImpactView>()

        verify(exactly = 0) { view.showPriceImpact(capture(priceImpactStates)) }

        assertEquals(0, priceImpactStates.size)

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check setAmountFiat IS NOT CALLED`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        presenter.attach(view)
        advanceUntilIdle()

        verify(exactly = 0) { view.setAmountFiat(any()) }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check showSolErrorToast IS NOT CALLED`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        presenter.attach(view)
        advanceUntilIdle()

        verify(exactly = 0) { view.showSolErrorToast() }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check closeScreen IS NOT CALLED`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        presenter.attach(view)
        advanceUntilIdle()

        verify(exactly = 0) { view.closeScreen() }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check openChangeTokenAScreen IS NOT CALLED`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        presenter.attach(view)
        advanceUntilIdle()

        verify(exactly = 0) { view.openChangeTokenAScreen() }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check openChangeTokenBScreen IS NOT CALLED`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        presenter.attach(view)
        advanceUntilIdle()

        verify(exactly = 0) { view.openChangeTokenBScreen() }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check showProgressDialog IS NOT CALLED`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        presenter.attach(view)
        advanceUntilIdle()

        verify(exactly = 0) { view.showProgressDialog(any(), any()) }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check showDefaultSlider IS NOT CALLED`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        presenter.attach(view)
        advanceUntilIdle()

        verify(exactly = 0) { view.showDefaultSlider() }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check showFullScreenError IS NOT CALLED`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        presenter.attach(view)
        advanceUntilIdle()

        verify(exactly = 0) { view.showFullScreenError() }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check hideFullScreenError IS NOT CALLED`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        presenter.attach(view)
        advanceUntilIdle()

        verify(exactly = 0) { view.hideFullScreenError() }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check setFirstTokenWidgetState values`() = runTest {
        val (firstToken, _, presenter) = createPresenterAndTokens()
        presenter.attach(view)
        advanceUntilIdle()

        // collecting view calls
        val firstTokenStates = mutableListQueueOf<SwapWidgetModel>()

        // main data
        verify(exactly = 1) { view.setFirstTokenWidgetState(capture(firstTokenStates)) }

        // now we have loaded tokens
        assertTrue(firstTokenStates.isNotEmpty())

        // verify content of the first widget
        assertTrue(firstTokenStates.back() is SwapWidgetModel.Content)
        checkFirstSwapWidgetModel(firstToken, firstTokenStates.back(), "")

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check setSecondTokenWidgetState state`() = runTest {
        val (_, secondToken, presenter) = createPresenterAndTokens()
        view.attachCallsLog()
        // we do only attaching to view
        presenter.attach(view)

        val secondTokenStates = mutableListQueueOf<SwapWidgetModel>()

        verify(exactly = 1) { view.setSecondTokenWidgetState(capture(secondTokenStates)) }
        advanceUntilIdle()

        assertTrue(secondTokenStates.isNotEmpty())

        // verify content of the second widget
        checkSecondSwapWidgetModel(secondToken, secondTokenStates.back())

        presenter.detach()
    }

    /**
     * I'm not sure if this test does make sense, as we just testing TextWatcher
     */
    @Test
    fun `GIVEN swap screen WHEN has set initialAmountA THEN check setFirstTokenWidget state has this value`() = runTest {
        val firstToken = JupiterSwapTestHelpers.createUSDCToken(BigDecimal("10.28"))
        val secondToken = JupiterSwapTestHelpers.createSOLToken(
            amount = BigDecimal("26.48"),
            rateToUsd = BigDecimal("22.14")
        )
        val initialAmount = firstToken.getFormattedTotal()

        val presenter = createPresenter {
            homeRepoAllTokens = mutableListOf(firstToken, secondToken)
            homeRepoUserTokens = homeRepoAllTokens
            initialAmountA = initialAmount
        }

        // we do only attaching to view
        presenter.attach(view)
        // simulate view notified presenter that value has changed
        presenter.onTokenAmountChange(initialAmount)
        advanceUntilIdle()

        val tokenStates = mutableListQueueOf<SwapWidgetModel>()

        verify(atLeast = 1) { view.setFirstTokenWidgetState(capture(tokenStates)) }

        // we have one more call
        assertTrue(tokenStates.isNotEmpty())

        assertTrue(tokenStates.back() is SwapWidgetModel.Content)
        checkFirstSwapWidgetModel(firstToken, tokenStates.back(), initialAmount)

        presenter.detach()
    }

    /* @todo DOESN'T WORK YET, NEEDS FIX in PreinstallTokensBySymbolSelector
    @Test
    fun `GIVEN swap screen WHEN has set initial tokenA and tokenB symbols THEN check token widget states`() = runTest {
        val firstToken = JupiterSwapTestHelpers.createUSDCToken(BigDecimal("10.28"))
        val secondToken = JupiterSwapTestHelpers.createSOLToken(
            amount = BigDecimal("26.48"),
            rateToUsd = BigDecimal("22.14")
        )
        val initialAmount = firstToken.getFormattedTotal()

        val presenter = createPresenter {
            homeRepoAllTokens = mutableListOf(firstToken, secondToken)
            homeRepoUserTokens = homeRepoAllTokens
            initialTokenASymbol = secondToken.tokenSymbol
            initialTokenBSymbol = firstToken.tokenSymbol
        }

        // we do only attaching to view
        presenter.attach(view)
        advanceUntilIdle()

        val firstTokenStates = mutableListQueueOf<SwapWidgetModel>()
        val secondTokenStates = mutableListQueueOf<SwapWidgetModel>()

        verify(atLeast = 1) { view.setFirstTokenWidgetState(capture(firstTokenStates)) }
        verify(atLeast = 1) { view.setSecondTokenWidgetState(capture(secondTokenStates)) }

        assertTrue(firstTokenStates.isNotEmpty())
        assertTrue(secondTokenStates.isNotEmpty())

        checkFirstSwapWidgetModel(secondToken, firstTokenStates.back(), "", availableAmountNullable = true)
        checkSecondSwapWidgetModel(firstToken, secondTokenStates.back())

        firstTokenStates;
        secondTokenStates;

        presenter.detach()
    }

     */
}
