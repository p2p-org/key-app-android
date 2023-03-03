package org.p2p.wallet.swap.jupiter.statemanager

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.utils.fromLamports
import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
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
        val lamportsTokenA: BigInteger,
        val lamportsTokenB: BigInteger,
        val routes: List<JupiterSwapRoute>,
        val activeRoute: Int,
        val jupiterSwapTransaction: Base64String,
        val slippage: Double
    ) : SwapState {

        val amountTokenA: BigDecimal
            get() = lamportsTokenA.fromLamports(tokenA.decimals)

        val amountTokenB: BigDecimal
            get() = lamportsTokenB.fromLamports(tokenB.decimals)
    }

    sealed interface SwapException : SwapState {

        val previousFeatureState: SwapState

        data class FeatureExceptionWrapper(
            override val previousFeatureState: SwapState,
            val featureException: SwapFeatureException,
        ) : SwapException

        data class OtherException(
            override val previousFeatureState: SwapState,
            val exception: Throwable,
        ) : SwapException
    }
}
