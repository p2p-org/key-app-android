package org.p2p.wallet.jupiter.ui.main

import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.junit.ClassRule
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.math.BigDecimal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.p2p.core.network.data.ErrorCode
import org.p2p.core.network.data.ServerException
import org.p2p.wallet.jupiter.interactor.JupiterSwapTokensResult
import org.p2p.wallet.jupiter.ui.main.JupiterSwapTestHelpers.attachCallsLog
import org.p2p.wallet.transaction.ui.SwapTransactionBottomSheetData
import org.p2p.wallet.utils.CoroutineExtension
import org.p2p.wallet.utils.SpyOnInjectMockKsExtension
import org.p2p.wallet.utils.TimberUnitTestInstance
import org.p2p.wallet.utils.back
import org.p2p.wallet.utils.mutableListQueueOf

@ExperimentalCoroutinesApi
@ExtendWith(SpyOnInjectMockKsExtension::class, CoroutineExtension::class)
class JupiterSwapPresenterExecuteTransactionTest : JupiterSwapPresenterBaseTest() {

    companion object {
        @ClassRule
        @JvmField
        val timber = TimberUnitTestInstance(
            isEnabled = false,
            defaultTag = "Swap:ExecuteTransaction",
            excludeMessages = listOf(
                "kotlinx.coroutines.JobCancellationException"
            ),
            excludeStacktraceForMessages = listOf(
                // cause error messages
                "Expected error",
                "statusCode: SLIPPAGE_LIMIT, errorMessage: Low slippage"
            )
        )
    }

    @Test
    fun `GIVEN swap screen WHEN amount is not set and slider swiped THEN check transaction is NOT executed`() = runTest {
        val firstToken = JupiterSwapTestHelpers.createUSDCToken(BigDecimal("10.28"))
        val secondToken = JupiterSwapTestHelpers.createSOLToken(
            amount = BigDecimal("26.48"),
            rateToUsd = BigDecimal("22.14")
        )

        val presenter = createPresenter {
            homeRepoAllTokens = mutableListOf(firstToken, secondToken)
            homeRepoUserTokens = homeRepoAllTokens
            jupiterSwapInteractorSwapTokens = { base64String ->
                JupiterSwapTokensResult.Success(base64String.base64Value)
            }
        }
        presenter.attach(view)
        advanceUntilIdle()
        presenter.onSwapSliderClicked()
        advanceUntilIdle()

        val buttonStates = mutableListQueueOf<SwapButtonState>()

        verify { view.setButtonState(capture(buttonStates)) }
        verify(exactly = 0) { view.showProgressDialog(any(), any()) }
        verify(exactly = 0) { view.showDefaultSlider() }

        checkButtonStateIsDisabledEnterAmount(buttonStates.back())

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN amount is set and slider swiped THEN check transaction is executed`() = runTest {
        val firstToken = JupiterSwapTestHelpers.createUSDCToken(BigDecimal("10.28"))
        val secondToken = JupiterSwapTestHelpers.createSOLToken(
            amount = BigDecimal("26.48"),
            rateToUsd = BigDecimal("22.14")
        )

        every { swapRoutesRefreshFeatureToggle.durationInMilliseconds } returns 10000L

        val presenter = createPresenter {
            homeRepoAllTokens = mutableListOf(firstToken, secondToken)
            homeRepoUserTokens = homeRepoAllTokens
            jupiterSwapInteractorSwapTokens = { base64String ->
                JupiterSwapTokensResult.Success(base64String.base64Value)
            }
        }
        presenter.attach(view)
        presenter.onTokenAmountChange("0.00000000000000015")
        advanceUntilIdle()
        presenter.onSwapSliderClicked()
        advanceUntilIdle()

        val transactionId = slot<String>()
        val transactionProgressData = slot<SwapTransactionBottomSheetData>()
        val buttonStates = mutableListQueueOf<SwapButtonState>()

        verify { view.setButtonState(capture(buttonStates)) }
        verify(exactly = 1) { view.showProgressDialog(capture(transactionId), capture(transactionProgressData)) }
        verify(exactly = 1) { view.showDefaultSlider() }

        checkButtonStateIsReadyToSwap(buttonStates.back(), firstToken.tokenSymbol, secondToken.tokenSymbol)

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN slider swiped and slippage is wrong THEN check transaction is not executed`() = runTest {
        val firstToken = JupiterSwapTestHelpers.createUSDCToken(BigDecimal("10.28"))
        val secondToken = JupiterSwapTestHelpers.createSOLToken(
            amount = BigDecimal("26.48"),
            rateToUsd = BigDecimal("22.14")
        )

        every { swapRoutesRefreshFeatureToggle.durationInMilliseconds } returns 10000L

        val presenter = createPresenter {
            homeRepoAllTokens = mutableListOf(firstToken, secondToken)
            homeRepoUserTokens = homeRepoAllTokens
            jupiterSwapInteractorSwapTokens = { _ ->
                JupiterSwapTokensResult.Failure(
                    JupiterSwapTokensResult.Failure.LowSlippageRpcError(
                        ServerException(ErrorCode.SLIPPAGE_LIMIT, "Low slippage", null)
                    )
                )
            }
        }
        view.attachCallsLog()
        presenter.attach(view)
        presenter.onTokenAmountChange("0.00000000000000015")
        advanceUntilIdle()
        presenter.onSwapSliderClicked()
        advanceUntilIdle()

        val transactionId = slot<String>()
        val transactionProgressData = slot<SwapTransactionBottomSheetData>()
        val buttonStates = mutableListQueueOf<SwapButtonState>()

        verify { view.setButtonState(capture(buttonStates)) }
        verify(exactly = 1) { view.showProgressDialog(capture(transactionId), capture(transactionProgressData)) }
        verify(exactly = 1) { view.showDefaultSlider() }

        checkButtonStateIsReadyToSwap(buttonStates.back(), firstToken.tokenSymbol, secondToken.tokenSymbol)

        presenter.detach()
    }

    @Test
    fun `GIVEN swap screen WHEN slider swapped and unknown error happened THEN check transaction is not executed`() = runTest {
        val firstToken = JupiterSwapTestHelpers.createUSDCToken(BigDecimal("10.28"))
        val secondToken = JupiterSwapTestHelpers.createSOLToken(
            amount = BigDecimal("26.48"),
            rateToUsd = BigDecimal("22.14")
        )

        every { swapRoutesRefreshFeatureToggle.durationInMilliseconds } returns 10000L

        val presenter = createPresenter {
            homeRepoAllTokens = mutableListOf(firstToken, secondToken)
            homeRepoUserTokens = homeRepoAllTokens
            jupiterSwapInteractorSwapTokens = { _ ->
                JupiterSwapTokensResult.Failure(RuntimeException("Expected error"))
            }
        }
        presenter.attach(view)
        presenter.onTokenAmountChange("0.00000000000000015")
        advanceUntilIdle()
        presenter.onSwapSliderClicked()
        advanceUntilIdle()

        val transactionId = slot<String>()
        val transactionProgressData = slot<SwapTransactionBottomSheetData>()
        val buttonStates = mutableListQueueOf<SwapButtonState>()

        verify { view.setButtonState(capture(buttonStates)) }
        verify(exactly = 1) { view.showProgressDialog(capture(transactionId), capture(transactionProgressData)) }
        verify(exactly = 1) { view.showDefaultSlider() }

        checkButtonStateIsReadyToSwap(buttonStates.back(), firstToken.tokenSymbol, secondToken.tokenSymbol)

        presenter.detach()
    }
}
