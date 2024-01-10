package org.p2p.wallet.jupiter.statemanager

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.crypto.Base64String
import org.p2p.core.utils.fromLamports
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.swap.model.Slippage

sealed interface SwapState {
    object InitialLoading : SwapState

    data class TokenAZero(
        val tokenA: SwapTokenModel,
        val tokenB: SwapTokenModel,
        val slippage: Slippage
    ) : SwapState

    data class TokenANotZero(
        val tokenA: SwapTokenModel,
        val tokenB: SwapTokenModel,
        val amountTokenA: BigDecimal,
        val slippage: Slippage
    ) : SwapState

    /**
     * Routes for selected pair are being fetched
     */
    data class LoadingRoutes(
        val tokenA: SwapTokenModel,
        val tokenB: SwapTokenModel,
        val amountTokenA: BigDecimal,
        val slippage: Slippage
    ) : SwapState

    /**
     * Routes are fetched, but transaction and simulation are not ready
     */
    data class RoutesLoaded(
        val tokenA: SwapTokenModel,
        val tokenB: SwapTokenModel,
        val amountTokenA: BigDecimal,
        val routes: List<JupiterSwapRoute>,
        val activeRouteIndex: Int,
        val amountTokenB: BigDecimal,
        val slippage: Slippage
    ) : SwapState

    /**
     * Transaction for the loaded route (activeRouteIndex) is creating and simulating
     */
    data class LoadingTransaction(
        val tokenA: SwapTokenModel,
        val tokenB: SwapTokenModel,
        val amountTokenA: BigDecimal,
        val routes: List<JupiterSwapRoute>,
        val activeRouteIndex: Int,
        val amountTokenB: BigDecimal,
        val slippage: Slippage
    ) : SwapState

    /**
     * Final successful step when transaction is created and simulated for the selected route
     */
    data class SwapLoaded(
        val tokenA: SwapTokenModel,
        val tokenB: SwapTokenModel,
        val lamportsTokenA: BigInteger,
        val lamportsTokenB: BigInteger,
        val routes: List<JupiterSwapRoute>,
        val activeRouteIndex: Int,
        val jupiterSwapTransaction: Base64String,
        val slippage: Slippage
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
            val lastSwapStateAction: SwapStateAction,
        ) : SwapException
    }
}

val SwapState.activeRoute: JupiterSwapRoute?
    get() = when (this) {
        SwapState.InitialLoading,
        is SwapState.LoadingRoutes,
        is SwapState.TokenANotZero,
        is SwapState.TokenAZero -> null

        is SwapState.SwapException -> previousFeatureState.activeRoute

        is SwapState.LoadingTransaction -> routes.getOrNull(activeRouteIndex)
        is SwapState.RoutesLoaded -> routes.getOrNull(activeRouteIndex)
        is SwapState.SwapLoaded -> routes.getOrNull(activeRouteIndex)
    }

val SwapState.currentSlippage: Slippage?
    get() = when (this) {
        SwapState.InitialLoading -> SwapStateManager.DEFAULT_SLIPPAGE
        is SwapState.LoadingRoutes -> slippage
        is SwapState.LoadingTransaction -> slippage
        is SwapState.SwapLoaded -> slippage
        is SwapState.TokenAZero -> slippage
        is SwapState.TokenANotZero -> slippage
        is SwapState.RoutesLoaded -> slippage
        is SwapState.SwapException -> null
    }

val SwapState.tokenAAmount: BigDecimal?
    get() {
        if (this is SwapState.SwapException && previousFeatureState !is SwapState.SwapException) {
            return previousFeatureState.tokenAAmount
        }
        val tokenA = when (this) {
            is SwapState.LoadingRoutes -> tokenA
            is SwapState.LoadingTransaction -> tokenA
            is SwapState.SwapLoaded -> tokenA
            is SwapState.TokenAZero -> tokenA
            is SwapState.TokenANotZero -> tokenA
            is SwapState.RoutesLoaded -> tokenA

            SwapState.InitialLoading,
            is SwapState.SwapException -> null
        }
        return (tokenA as? SwapTokenModel.UserToken)?.details?.total
    }
