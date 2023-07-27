package org.p2p.wallet.jupiter.interactor

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.utils.isLessThan
import org.p2p.core.crypto.Base64String
import org.p2p.wallet.jupiter.interactor.model.SwapPriceImpactType
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.statemanager.SwapState
import org.p2p.wallet.jupiter.statemanager.activeRoute
import org.p2p.wallet.jupiter.statemanager.currentSlippage
import org.p2p.wallet.swap.model.Slippage
import org.p2p.core.utils.divideSafe

private const val TAG = "JupiterSwapInteractor"

class JupiterSwapInteractor(
    private val swapSendSwapTransactionDelegate: JupiterSwapSendTransactionDelegate
) {

    suspend fun swapTokens(
        swapRoute: JupiterSwapRoute,
        jupiterTransaction: Base64String
    ): JupiterSwapTokensResult = swapSendSwapTransactionDelegate.sendSwapTransaction(swapRoute, jupiterTransaction)

    fun getSwapTokenPair(state: SwapState): Pair<SwapTokenModel?, SwapTokenModel?> = state.run {
        when (this) {
            SwapState.InitialLoading -> null to null

            is SwapState.LoadingRoutes -> tokenA to tokenB
            is SwapState.LoadingTransaction -> tokenA to tokenB
            is SwapState.SwapLoaded -> tokenA to tokenB
            is SwapState.TokenAZero -> tokenA to tokenB
            is SwapState.TokenANotZero -> tokenA to tokenB
            is SwapState.RoutesLoaded -> tokenA to tokenB

            is SwapState.SwapException -> getSwapTokenPair(previousFeatureState)
        }
    }

    /**
     * Returns price impact for current swap state and active route
     */
    fun getPriceImpact(state: SwapState?): SwapPriceImpactType {
        state ?: return SwapPriceImpactType.None
        val activeRoute: JupiterSwapRoute = state.activeRoute ?: return SwapPriceImpactType.None
        val currentSlippage: Slippage = state.currentSlippage ?: return SwapPriceImpactType.None

        val priceImpactByFee = checkForHighFees(
            keyAppFeeLamports = activeRoute.keyAppFeeInLamports,
            outAmountLamports = activeRoute.outAmountInLamports,
            slippage = currentSlippage
        )

        val priceImpactPercent: BigDecimal = activeRoute.priceImpactPct
        val threePercent = BigDecimal.valueOf(0.03)
        val onePercent = BigDecimal.valueOf(0.01)

        val priceImpact = when {
            priceImpactPercent.isLessThan(onePercent) -> {
                SwapPriceImpactType.None
            }
            priceImpactPercent.isLessThan(threePercent) -> {
                SwapPriceImpactType.HighPriceImpact(
                    priceImpactValue = priceImpactPercent,
                    type = SwapPriceImpactType.HighPriceImpactType.YELLOW
                )
            }
            else -> {
                SwapPriceImpactType.HighPriceImpact(
                    priceImpactValue = priceImpactPercent,
                    type = SwapPriceImpactType.HighPriceImpactType.RED
                )
            }
        }

        // price impact by fee has lower priority
        return if (priceImpact != SwapPriceImpactType.None) priceImpact else priceImpactByFee
    }

    /**
     * Check if price impact is high due to high fees and exceeds given slippage
     *
     * Since we getting BigInteger values, we need to convert them to BigDecimal to be able to get fraction.
     * Example: dividing 2039280 / 955187 returns 2 instead of 2.13
     *
     * @param keyAppFeeLamports key app fee in lamports
     * @param outAmountLamports out amount in lamports
     * @return [SwapPriceImpactType.HighFees] if price impact is more than given slippage,
     * [SwapPriceImpactType.None] otherwise
     */
    private fun checkForHighFees(
        keyAppFeeLamports: BigInteger,
        outAmountLamports: BigInteger,
        slippage: Slippage
    ): SwapPriceImpactType {
        val keyAppFee = keyAppFeeLamports.toBigDecimal()
        val outAmount = outAmountLamports.toBigDecimal()
        return if (keyAppFee.divideSafe(outAmount + keyAppFee) > slippage.doubleValue.toBigDecimal()) {
            SwapPriceImpactType.HighFees(slippage)
        } else {
            SwapPriceImpactType.None
        }
    }
}
