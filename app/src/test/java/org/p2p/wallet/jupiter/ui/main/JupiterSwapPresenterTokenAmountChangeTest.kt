package org.p2p.wallet.jupiter.ui.main

import io.mockk.every
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.ClassRule
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.core.common.TextContainer
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.jupiter.ui.main.JupiterSwapTestHelpers.attachCallsLog
import org.p2p.wallet.jupiter.ui.main.widget.SwapWidgetModel
import org.p2p.wallet.utils.CoroutineExtension
import org.p2p.wallet.utils.SpyOnInjectMockKsExtension
import org.p2p.wallet.utils.TimberUnitTestInstance
import org.p2p.wallet.utils.back
import org.p2p.wallet.utils.front
import org.p2p.wallet.utils.mutableListQueueOf

@ExperimentalCoroutinesApi
@ExtendWith(SpyOnInjectMockKsExtension::class, CoroutineExtension::class)
class JupiterSwapPresenterTokenAmountChangeTest : JupiterSwapPresenterBaseTest() {

    companion object {
        @ClassRule
        @JvmField
        val timber = TimberUnitTestInstance(
            isEnabled = false,
            defaultTag = "Swap:TokenAmountChange",
            excludeMessages = listOf(
                "kotlinx.coroutines.JobCancellationException"
            ),
            excludeStacktraceForMessages = listOf(
                "NotValidTokenA"
            )
        )
    }

    // @Test
    fun `GIVEN swap screen WHEN onTokenAmountChange more than balance THEN check swap button disabled and message not enough balance`() =
        runTest {
            val (firstToken, _, presenter) = createPresenterAndTokens()
            view.attachCallsLog()
            presenter.attach(view)
            presenter.onTokenAmountChange((firstToken.total + BigDecimal("1")).toPlainString())
            advanceUntilIdle()

            val buttonStates = mutableListQueueOf<SwapButtonState>()
            verify { view.setButtonState(capture(buttonStates)) }

            checkButtonStateIsNotEnoughAmount(buttonStates.back(), firstToken.tokenSymbol)

            presenter.detach()
        }

    @Test
    fun `GIVEN swap screen WHEN onTokenAmountChange equal to balance THEN check swap button enabled`() = runTest {
        every { swapRoutesRefreshFeatureToggle.durationInMilliseconds } returns 10000L

        val (firstToken, secondToken, presenter) = createPresenterAndTokens()
        view.attachCallsLog()
        presenter.attach(view)
        presenter.onTokenAmountChange((firstToken.total).toPlainString())
        advanceUntilIdle()

        val buttonStates = mutableListQueueOf<SwapButtonState>()
        verify(atLeast = 3) { view.setButtonState(capture(buttonStates)) }

        checkButtonStateIsDisabledEnterAmount(buttonStates.front())
        checkButtonStateIsDisabledCounting(buttonStates[buttonStates.size - 2])
        checkButtonStateIsReadyToSwap(buttonStates.back(), firstToken.tokenSymbol, secondToken.tokenSymbol)

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN onTokenAmountChange set zero THEN check swap button disabled and message enter balance`() =
        runTest {
            val (_, _, presenter) = createPresenterAndTokens()
            view.attachCallsLog()
            presenter.attach(view)
            advanceUntilIdle()
            presenter.onTokenAmountChange("0")
            advanceUntilIdle()

            val buttonStates = mutableListQueueOf<SwapButtonState>()
            verify { view.setButtonState(capture(buttonStates)) }

            checkButtonStateIsDisabledEnterAmount(buttonStates.back())

            presenter.detach()
        }

    @Test
    fun `GIVEN swap screen WHEN onTokenAmountChange set very small amount THEN check swap button enabled`() = runTest {
        val (firstToken, secondToken, presenter) = createPresenterAndTokens()
        view.attachCallsLog()
        presenter.attach(view)
        presenter.onTokenAmountChange("0.00000000000000015")
        advanceUntilIdle()

        val buttonStates = mutableListQueueOf<SwapButtonState>()
        // we have only 2 calls, because state has not changed since balances loaded
        verify { view.setButtonState(capture(buttonStates)) }

        checkButtonStateIsReadyToSwap(buttonStates.back(), firstToken.tokenSymbol, secondToken.tokenSymbol)

        presenter.detach()
    }

    /**
     * TODO: fix this test and behavior
     */
    @Test
    fun `GIVEN swap screen WHEN onTokenAmountChange set negative amount THEN check swap button enabled negative conversion`() =
        runTest {
            val (firstToken, secondToken, presenter) = createPresenterAndTokens()
            view.attachCallsLog()
            presenter.attach(view)
            presenter.onTokenAmountChange("-1")
            advanceUntilIdle()

            val firstTokenStates = mutableListQueueOf<SwapWidgetModel>()
            val buttonStates = mutableListQueueOf<SwapButtonState>()

            verify(atLeast = 1) { view.setButtonState(capture(buttonStates)) }
            verify(atLeast = 1) { view.setFirstTokenWidgetState(capture(firstTokenStates)) }

            checkButtonStateIsReadyToSwap(buttonStates.back(), firstToken.tokenSymbol, secondToken.tokenSymbol)

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
    fun `GIVEN swap screen WHEN onTokenAmountChange set NaN THEN check swap button disabled and message enter amount`() =
        runTest {
            val (_, _, presenter) = createPresenterAndTokens()
            view.attachCallsLog()
            presenter.attach(view)
            presenter.onTokenAmountChange("all hell breaks loose")
            advanceUntilIdle()

            val buttonStates = mutableListQueueOf<SwapButtonState>()
            // we have only 2 calls, because state has not changed since balances loaded
            verify { view.setButtonState(capture(buttonStates)) }

            checkButtonStateIsDisabledEnterAmount(buttonStates.back())

            presenter.detach()
        }

    @Test
    fun `GIVEN swap screen WHEN onTokenAmountChange set emoji THEN check swap button disabled and message enter amount`() =
        runTest {
            val (_, _, presenter) = createPresenterAndTokens()
            view.attachCallsLog()
            presenter.attach(view)
            presenter.onTokenAmountChange("👻 🤖 🎃")
            advanceUntilIdle()

            val buttonStates = mutableListQueueOf<SwapButtonState>()
            // we have only 2 calls, because state has not changed since balances loaded
            verify { view.setButtonState(capture(buttonStates)) }

            checkButtonStateIsDisabledEnterAmount(buttonStates.back())

            presenter.detach()
        }

    @Test
    fun `GIVEN swap screen WHEN onAllAmountClick() clicked THEN check swap button disabled and message enter amount`() = runTest {
        val firstToken = JupiterSwapTestHelpers.createUSDCToken(BigDecimal("10.30"))
        val secondToken = JupiterSwapTestHelpers.createSOLToken(
            amount = BigDecimal("26.48"),
            rateToUsd = BigDecimal("20.74")
        )

        every { swapRoutesRefreshFeatureToggle.durationInMilliseconds } returns 10000L

        val presenter = createPresenter {
            homeRepoAllTokens = mutableListOf(firstToken, secondToken)
            homeRepoUserTokens = homeRepoAllTokens
            jupiterSwapRoutesRepoGetSwapRoutesForSwapPair = { pair, pk ->
                listOf(
                    JupiterSwapTestHelpers.createSwapRoute(
                        TestSwapRouteData(
                            swapPair = pair,
                            userPublicKey = pk,
                            ratio = JupiterSwapTestHelpers.getRateFromUsd(secondToken.rate!!),
                            inDecimals = firstToken.decimals,
                            outDecimals = secondToken.decimals
                        )
                    )
                )
            }
        }
        presenter.attach(view)
        presenter.onAllAmountClick()
        advanceUntilIdle()

        val buttonStates = mutableListQueueOf<SwapButtonState>()
        val firstTokenStates = mutableListQueueOf<SwapWidgetModel>()
        val secondTokenStates = mutableListQueueOf<SwapWidgetModel>()

        verify(atLeast = 3) { view.setButtonState(capture(buttonStates)) }
        verify(atLeast = 1) { view.setFirstTokenWidgetState(capture(firstTokenStates)) }
        verify(atLeast = 1) { view.setSecondTokenWidgetState(capture(secondTokenStates)) }

        // total - because we used onAllAmount
        // rate 1 SOL = 20.74 USD
        // 10.28 / 20.74
        // ≈ 0.495660559
        val expectedOutAmount = firstToken.total.setScale(firstToken.decimals)
            .divide(secondToken.rate!!, secondToken.decimals, RoundingMode.DOWN)
            .toString()

        checkButtonStateIsDisabledEnterAmount(buttonStates.front())
        checkButtonStateIsDisabledCounting(buttonStates[buttonStates.size - 2])
        checkButtonStateIsReadyToSwap(buttonStates.back(), firstToken.tokenSymbol, secondToken.tokenSymbol)

        checkFirstSwapWidgetModel(firstToken, firstTokenStates.back(), firstToken.getFormattedTotal())
        checkSecondSwapWidgetModel(secondToken, secondTokenStates.back(), expectedOutAmount)

        presenter.detach()
    }
}
