package org.p2p.wallet.jupiter.ui.main

import io.mockk.coVerify
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.core.common.TextContainer
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.R
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel
import org.p2p.wallet.utils.CoroutineExtension
import org.p2p.wallet.utils.SpyOnInjectMockKsExtension
import org.p2p.wallet.utils.back
import org.p2p.wallet.utils.mutableListQueueOf
import org.p2p.wallet.utils.plantTimberToStdout

@ExperimentalCoroutinesApi
@ExtendWith(SpyOnInjectMockKsExtension::class, CoroutineExtension::class)
class JupiterSwapPresenterTokenAmountChangeTest : JupiterSwapPresenterBaseTest() {

    init {
        plantTimberToStdout(
            defaultTag = "Swap:TokenAmountChange",
            excludeMessages = listOf(
                "kotlinx.coroutines.JobCancellationException"
            ),
            excludeStacktraceForMessages = listOf(
                "NotValidTokenA"
            )
        )
    }

    @Test
    fun `GIVEN swap screen WHEN onTokenAmountChange more than balance THEN check swap button disabled and message not enough balance`() = runTest {
        val (firstToken, _, presenter) = createPresenterAndTokens()
        JupiterSwapTestHelpers.attachLogToViewCalls(view)
        presenter.attach(view)
        advanceUntilIdle()

        presenter.onTokenAmountChange((firstToken.total + BigDecimal("1")).toPlainString())
        advanceUntilIdle()

        val buttonStates = mutableListQueueOf<SwapButtonState>()
        coVerify(exactly = 3) { view.setButtonState(capture(buttonStates)) }

        assertTrue(buttonStates.back() is SwapButtonState.Disabled)
        with(buttonStates.back() as SwapButtonState.Disabled) {
            val container = text as TextContainer.ResParams
            assertEquals(R.string.swap_main_button_not_enough_amount, container.textRes)
            assertEquals(1, container.args.size)
            assertEquals(firstToken.tokenSymbol, container.args.first())
        }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN onTokenAmountChange equal to balance THEN check swap button enabled`() = runTest {
        val (firstToken, secondToken, presenter) = createPresenterAndTokens()
        JupiterSwapTestHelpers.attachLogToViewCalls(view)
        presenter.attach(view)
        advanceUntilIdle()
        presenter.onTokenAmountChange((firstToken.total).toPlainString())
        advanceUntilIdle()

        val buttonStates = mutableListQueueOf<SwapButtonState>()
        coVerify(exactly = 3) { view.setButtonState(capture(buttonStates)) }

        assertTrue(buttonStates.back() is SwapButtonState.ReadyToSwap)
        with(buttonStates.back() as SwapButtonState.ReadyToSwap) {
            val container = text as TextContainer.ResParams
            assertEquals(R.string.swap_main_button_ready_to_swap, container.textRes)
            assertEquals(2, container.args.size)
            assertEquals(firstToken.tokenSymbol, container.args[0])
            assertEquals(secondToken.tokenSymbol, container.args[1])
        }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN onTokenAmountChange set zero THEN check swap button disabled and message enter balance`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        JupiterSwapTestHelpers.attachLogToViewCalls(view)
        presenter.attach(view)
        advanceUntilIdle()
        presenter.onTokenAmountChange("0")
        advanceUntilIdle()

        val buttonStates = mutableListQueueOf<SwapButtonState>()
        // we have only 2 calls, because state has not changed since balances loaded
        coVerify(exactly = 2) { view.setButtonState(capture(buttonStates)) }

        assertTrue(buttonStates.back() is SwapButtonState.Disabled)
        with(buttonStates.back() as SwapButtonState.Disabled) {
            val container = text as TextContainer.Res
            assertEquals(R.string.swap_main_button_enter_amount, container.textRes)
        }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN onTokenAmountChange set very small amount THEN check swap button enabled`() = runTest {
        val (firstToken, secondToken, presenter) = createPresenterAndTokens()
        JupiterSwapTestHelpers.attachLogToViewCalls(view)
        presenter.attach(view)
        advanceUntilIdle()
        presenter.onTokenAmountChange("0.00000000000000015")
        advanceUntilIdle()

        val buttonStates = mutableListQueueOf<SwapButtonState>()
        // we have only 2 calls, because state has not changed since balances loaded
        coVerify(exactly = 3) { view.setButtonState(capture(buttonStates)) }

        assertTrue(buttonStates.back() is SwapButtonState.ReadyToSwap)
        with(buttonStates.back() as SwapButtonState.ReadyToSwap) {
            val container = text as TextContainer.ResParams
            assertEquals(R.string.swap_main_button_ready_to_swap, container.textRes)
            assertEquals(2, container.args.size)
            assertEquals(firstToken.tokenSymbol, container.args[0])
            assertEquals(secondToken.tokenSymbol, container.args[1])
        }

        presenter.detach()
    }

    /**
     * TODO: fix this test and behavior
     */
    @Test
    fun `GIVEN swap screen WHEN onTokenAmountChange set negative amount THEN check swap button enabled negative conversion`() = runTest {
        val (firstToken, secondToken, presenter) = createPresenterAndTokens()
        JupiterSwapTestHelpers.attachLogToViewCalls(view)
        presenter.attach(view)
        advanceUntilIdle()
        presenter.onTokenAmountChange("-1")
        advanceUntilIdle()

        val firstTokenStates = mutableListQueueOf<SwapWidgetModel>()
        val buttonStates = mutableListQueueOf<SwapButtonState>()

        verify(exactly = 3) { view.setButtonState(capture(buttonStates)) }
        verify(atLeast = 1) { view.setFirstTokenWidgetState(capture(firstTokenStates)) }

        assertTrue(buttonStates.back() is SwapButtonState.ReadyToSwap)
        with(buttonStates.back() as SwapButtonState.ReadyToSwap) {
            val container = text as TextContainer.ResParams
            assertEquals(R.string.swap_main_button_ready_to_swap, container.textRes)
            assertEquals(2, container.args.size)
            assertEquals(firstToken.tokenSymbol, container.args[0])
            assertEquals(secondToken.tokenSymbol, container.args[1])
        }

        assertTrue(firstTokenStates.back() is SwapWidgetModel.Content)
        with(firstTokenStates.back() as SwapWidgetModel.Content) {
            with(amount as TextViewCellModel.Raw) {
                val container = text as TextContainer.Raw
                // todo: it seems presenter should check negative numbers at the input
                assertEquals("-1", container.text)
            }
            with(fiatAmount as TextViewCellModel.Raw) {
                val container = text as TextContainer.Raw
                assertEquals("<0.01 USD", container.text)
            }
        }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN onTokenAmountChange set NaN THEN check swap button disabled and message enter balance`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        JupiterSwapTestHelpers.attachLogToViewCalls(view)
        presenter.attach(view)
        advanceUntilIdle()
        presenter.onTokenAmountChange("all hell breaks loose")
        advanceUntilIdle()

        val buttonStates = mutableListQueueOf<SwapButtonState>()
        // we have only 2 calls, because state has not changed since balances loaded
        verify(exactly = 2) { view.setButtonState(capture(buttonStates)) }

        assertTrue(buttonStates.back() is SwapButtonState.Disabled)
        with(buttonStates.back() as SwapButtonState.Disabled) {
            val container = text as TextContainer.Res
            assertEquals(R.string.swap_main_button_enter_amount, container.textRes)
        }

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN onTokenAmountChange set emoji THEN check swap button disabled and message enter balance`() = runTest {
        val (_, _, presenter) = createPresenterAndTokens()
        JupiterSwapTestHelpers.attachLogToViewCalls(view)
        presenter.attach(view)
        advanceUntilIdle()
        presenter.onTokenAmountChange("👻 🤖 🎃")
        advanceUntilIdle()

        val buttonStates = mutableListQueueOf<SwapButtonState>()
        // we have only 2 calls, because state has not changed since balances loaded
        coVerify(exactly = 2) { view.setButtonState(capture(buttonStates)) }

        assertTrue(buttonStates.back() is SwapButtonState.Disabled)
        with(buttonStates.back() as SwapButtonState.Disabled) {
            val container = text as TextContainer.Res
            assertEquals(R.string.swap_main_button_enter_amount, container.textRes)
        }

        presenter.detach()
    }
}
