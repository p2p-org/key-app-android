package org.p2p.wallet.swap.jupiter.statemanager

import java.math.BigDecimal
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapRoute

sealed interface SwapState {
    object InitialLoading : SwapState

    data class TokenAZero(
        val tokenA: SwapTokenModel,
        val tokenB: SwapTokenModel,
        val slippage: Double
    ) : SwapState

    data class LoadingRoutes(
        val tokenA: SwapTokenModel,
        val tokenB: SwapTokenModel,
        val amountTokenA: BigDecimal,
        val slippage: Double
    ) : SwapState

    data class LoadingTransaction(
        val tokenA: SwapTokenModel,
        val tokenB: SwapTokenModel,
        val amountTokenA: BigDecimal,
        val routes: List<JupiterSwapRoute>,
        val activeRoute: Int,
        val amountTokenB: BigDecimal,
        val slippage: Double
    ) : SwapState

    data class SwapLoaded(
        val tokenA: SwapTokenModel,
        val tokenB: SwapTokenModel,
        val amountTokenA: BigDecimal,
        val amountTokenB: BigDecimal,
        val routes: List<JupiterSwapRoute>,
        val activeRoute: Int,
        val swapBlockchainTransaction: Base64String,
        val slippage: Double
    ) : SwapState

    sealed interface SwapException : SwapState {

        val previousFeatureState: SwapState

        data class FeatureExceptionWrapper(
            override val previousFeatureState: SwapState,
            val featureException: FeatureException,
        ) : SwapException

        data class OtherException(
            override val previousFeatureState: SwapState,
            val exception: Exception,
        ) : SwapException
    }
}
