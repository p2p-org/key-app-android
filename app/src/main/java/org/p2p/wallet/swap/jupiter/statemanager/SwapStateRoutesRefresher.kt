package org.p2p.wallet.swap.jupiter.statemanager

import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.toLamports
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapPair
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.swap.jupiter.repository.model.SwapFailure
import org.p2p.wallet.swap.jupiter.repository.routes.JupiterSwapRoutesRepository
import org.p2p.wallet.swap.jupiter.repository.transaction.JupiterSwapTransactionRepository
import org.p2p.wallet.swap.jupiter.statemanager.validator.SwapValidator
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.utils.toBase58Instance

class SwapStateRoutesRefresher(
    private val tokenKeyProvider: TokenKeyProvider,
    private val swapRoutesRepository: JupiterSwapRoutesRepository,
    private val swapTransactionRepository: JupiterSwapTransactionRepository,
    private val swapValidator: SwapValidator,
) {
    suspend fun refreshRoutes(
        state: MutableStateFlow<SwapState>,
        tokenA: SwapTokenModel,
        tokenB: SwapTokenModel,
        amountTokenA: BigDecimal,
        slippage: Slippage,
        activeRouteOrdinal: Int
    ) {
        swapValidator.validateInputAmount(tokenA = tokenA, amountTokenA = amountTokenA)
        swapValidator.validateIsSameTokens(tokenA = tokenA, tokenB = tokenB)
        state.value = SwapState.LoadingRoutes(
            tokenA = tokenA,
            tokenB = tokenB,
            amountTokenA = amountTokenA,
            slippage = slippage
        )

        val updatedRoutes = fetchRoutes(
            tokenA = tokenA,
            tokenB = tokenB,
            amountTokenA = amountTokenA,
            slippage = slippage
        )
        Timber.i("Jupiter routes fetched: ${updatedRoutes.size}")

        val bestRoute = updatedRoutes.getOrNull(activeRouteOrdinal)
            ?: throw SwapFeatureException.RoutesNotFound

        val amountTokenB = bestRoute
            .outAmountInLamports
            .fromLamports(tokenB.decimals)

        state.value = SwapState.LoadingTransaction(
            tokenA = tokenA,
            tokenB = tokenB,
            amountTokenA = amountTokenA,
            routes = updatedRoutes,
            activeRoute = activeRouteOrdinal,
            amountTokenB = amountTokenB,
            slippage = slippage,
        )

        val freshSwapTransaction = try {
            swapTransactionRepository.createSwapTransactionForRoute(
                route = updatedRoutes[activeRouteOrdinal],
                userPublicKey = tokenKeyProvider.publicKey.toBase58Instance()
            )
        } catch (error: SwapFailure.CreateSwapTransactionFailed) {
            state.value = SwapState.SwapException.OtherException(
                previousFeatureState = state.value,
                exception = error
            )
            return
        }
        Timber.i("Fresh swap transaction fetched")

        state.value = SwapState.SwapLoaded(
            tokenA = tokenA,
            tokenB = tokenB,
            lamportsTokenA = bestRoute.inAmountInLamports,
            lamportsTokenB = bestRoute.outAmountInLamports,
            routes = updatedRoutes,
            activeRoute = activeRouteOrdinal,
            jupiterSwapTransaction = freshSwapTransaction,
            slippage = slippage
        )
    }

    private suspend fun fetchRoutes(
        tokenA: SwapTokenModel,
        tokenB: SwapTokenModel,
        amountTokenA: BigDecimal,
        slippage: Slippage,
    ): List<JupiterSwapRoute> {
        val routesRequest = JupiterSwapPair(
            inputMint = tokenA.mintAddress,
            outputMint = tokenB.mintAddress,
            amountInLamports = amountTokenA.toLamports(tokenA.decimals),
            slippageBasePoints = (slippage.doubleValue * 10000).toInt() // 100% = 1000; 0.5 = 50
        )
        return swapRoutesRepository.getSwapRoutesForSwapPair(
            jupiterSwapPair = routesRequest,
            userPublicKey = tokenKeyProvider.publicKey.toBase58Instance()
        )
    }
}
