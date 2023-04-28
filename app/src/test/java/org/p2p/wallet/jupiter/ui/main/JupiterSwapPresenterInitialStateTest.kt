package org.p2p.wallet.jupiter.ui.main

import io.mockk.coVerify
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.core.common.TextContainer
import org.p2p.core.utils.formatFiat
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.statemanager.price_impact.SwapPriceImpactView
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel
import org.p2p.wallet.utils.CoroutineExtension
import org.p2p.wallet.utils.SpyOnInjectMockKsExtension
import org.p2p.wallet.utils.mutableListQueueOf
import org.p2p.wallet.utils.front
import org.p2p.wallet.utils.plantTimberToStdout
import org.p2p.wallet.utils.pop

@ExperimentalCoroutinesApi
@ExtendWith(SpyOnInjectMockKsExtension::class, CoroutineExtension::class)
class JupiterSwapPresenterInitialStateTest : JupiterSwapPresenterBaseTest() {

    init {
        plantTimberToStdout("Swap:InitialState")
    }

    @Before
    override fun setUp() {
        super.setUp()
    }

    @After
    override fun tearDown() {
        super.tearDown()
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check setRatioState values`() = runTest {
        val (firstToken, secondToken, presenter) = createPresenterAndTokens()
        // we do only attaching to view
        presenter.attach(view)
        advanceUntilIdle()

        val ratioStates = mutableListQueueOf<TextViewCellModel?>()

        verify(exactly = 2) { view.setRatioState(captureNullable(ratioStates)) }

        assertTrue(ratioStates.isNotEmpty())
        assertTrue(ratioStates.front() is TextViewCellModel.Skeleton)

        ratioStates.pop()

        // check rates SOL -> USDC
        assertTrue(ratioStates.isNotEmpty())
        assertTrue(ratioStates.front() is TextViewCellModel.Raw)
        with(ratioStates.front() as TextViewCellModel.Raw) {
            val (tokenA, tokenB) = JupiterSwapTestHelpers.getTokensInNonFiatToFiatOrder(firstToken, secondToken)
            val container = text as TextContainer.Raw
            // we pre-defined rates at the beginning of the test
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

        coVerify(exactly = 2) { view.setButtonState(capture(buttonStates)) }

        assertTrue(buttonStates.isNotEmpty())

        // why hide is initial state?
        assertEquals(SwapButtonState.Hide, buttonStates.front())

        buttonStates.pop()

        // check buttons is disabled as amount hasn't set
        assertTrue(buttonStates.isNotEmpty())
        assertTrue(buttonStates.front() is SwapButtonState.Disabled)
        with(buttonStates.front() as SwapButtonState.Disabled) {
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
        verify(exactly = 2) { view.setFirstTokenWidgetState(capture(firstTokenStates)) }

        assertTrue(firstTokenStates.isNotEmpty())

        assertTrue(firstTokenStates.front() is SwapWidgetModel.Loading)
        with(firstTokenStates.front() as SwapWidgetModel.Loading) {
            val container = widgetTitle.text as TextContainer.Res
            assertEquals(R.string.swap_main_you_pay, container.textRes)
        }

        // reset states and looking for next states
        firstTokenStates.pop()

        // now we have loaded tokens
        assertTrue(firstTokenStates.isNotEmpty())

        // verify content of the first widget
        assertTrue(firstTokenStates.front() is SwapWidgetModel.Content)
        with(firstTokenStates.front() as SwapWidgetModel.Content) {
            // "you pay"
            with(widgetTitle as TextViewCellModel.Raw) {
                val container = text as TextContainer.Res
                assertEquals(R.string.swap_main_you_pay, container.textRes)
            }
            // amount input
            with(amount as TextViewCellModel.Raw) {
                val container = text as TextContainer.Raw
                assertEquals("", container.text)
            }
            // all %s
            with(availableAmount as TextViewCellModel.Raw) {
                val container = text as TextContainer.Raw
                assertEquals(firstToken.getFormattedTotal(true), container.text)
            }
            // balance %s
            with(balance as TextViewCellModel.Raw) {
                val container = text as TextContainer.ResParams
                assertEquals(R.string.swap_main_balance_amount, container.textRes)
                assertEquals(1, container.args.size)
                assertEquals(firstToken.getFormattedTotal(), container.args.first() as String)
            }
            // token symbol
            with(currencyName as TextViewCellModel.Raw) {
                val textContainer = text as TextContainer.Raw
                assertEquals(firstToken.tokenSymbol, textContainer.text)
            }
            // check token decimals
            assertEquals(firstToken.decimals, amountMaxDecimals)
        }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN it is in initial state THEN check setSecondTokenWidgetState state`() = runTest {
        val (_, secondToken, presenter) = createPresenterAndTokens()
        // we do only attaching to view
        presenter.attach(view)
        advanceUntilIdle()
        val secondTokenStates = mutableListQueueOf<SwapWidgetModel>()

        verify(exactly = 2) { view.setSecondTokenWidgetState(capture(secondTokenStates)) }

        assertTrue(secondTokenStates.isNotEmpty())

        assertTrue(secondTokenStates.front() is SwapWidgetModel.Loading)
        run {
            val secondTokenText = (secondTokenStates.front() as SwapWidgetModel.Loading)
            val secondTokenWidgetText = secondTokenText.widgetTitle.text as TextContainer.Res
            assertEquals(R.string.swap_main_you_receive, secondTokenWidgetText.textRes)
        }

        // reset state
        secondTokenStates.pop()
        // we have one more call
        assertTrue(secondTokenStates.isNotEmpty())

        // verify content of the second widget
        assertTrue(secondTokenStates.front() is SwapWidgetModel.Content)

        with(secondTokenStates.front() as SwapWidgetModel.Content) {
            // "you receive"
            with(widgetTitle as TextViewCellModel.Raw) {
                val textContainer = text as TextContainer.Res
                assertEquals(R.string.swap_main_you_receive, textContainer.textRes)
            }
            // amount input (disabled)
            with(amount as TextViewCellModel.Raw) {
                val textContainer = text as TextContainer.Raw
                assertEquals("", textContainer.text)
            }
            // balance %s
            with(balance as TextViewCellModel.Raw) {
                val textContainer = text as TextContainer.ResParams
                assertEquals(R.string.swap_main_balance_amount, textContainer.textRes)
                assertEquals(1, textContainer.args.size)
                assertEquals(secondToken.getFormattedTotal(), textContainer.args.first() as String)
            }
            // all %s - not available for second token
            assertNull(availableAmount)

            with(currencyName as TextViewCellModel.Raw) {
                val textContainer = text as TextContainer.Raw
                assertEquals(secondToken.tokenSymbol, textContainer.text)
            }
            assertEquals(secondToken.decimals, amountMaxDecimals)
        }

        presenter.detach()
    }
}
