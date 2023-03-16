package org.p2p.wallet.jupiter.statemanager

import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.toLamports
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapPair
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.routes.JupiterSwapRoutesRepository
import org.p2p.wallet.jupiter.repository.transaction.JupiterSwapTransactionRepository
import org.p2p.wallet.jupiter.statemanager.validator.MinimumSolAmountValidator
import org.p2p.wallet.jupiter.statemanager.validator.SwapValidator
import org.p2p.wallet.swap.model.Slippage
import org.p2p.wallet.utils.toBase58Instance

class SwapStateRoutesRefresher(
    private val tokenKeyProvider: TokenKeyProvider,
    private val swapRoutesRepository: JupiterSwapRoutesRepository,
    private val swapTransactionRepository: JupiterSwapTransactionRepository,
    private val minSolBalanceValidator: MinimumSolAmountValidator,
    private val swapValidator: SwapValidator,
) {
    suspend fun refreshRoutes(
        state: MutableStateFlow<SwapState>,
        tokenA: SwapTokenModel,
        tokenB: SwapTokenModel,
        amountTokenA: BigDecimal,
        slippage: Slippage,
        activeRouteIndex: Int
    ) {

        minSolBalanceValidator.validateMinimumSolAmount(
            tokenA = tokenA,
            newAmount = amountTokenA,
            slippage = slippage
        )
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

        val activeRoute = updatedRoutes.getOrNull(activeRouteIndex)
            ?: throw SwapFeatureException.RoutesNotFound

        val amountTokenB = activeRoute
            .outAmountInLamports
            .fromLamports(tokenB.decimals)

        state.value = SwapState.LoadingTransaction(
            tokenA = tokenA,
            tokenB = tokenB,
            amountTokenA = amountTokenA,
            routes = updatedRoutes,
            activeRoute = activeRouteIndex,
            amountTokenB = amountTokenB,
            slippage = slippage,
        )

        val freshSwapTransaction = swapTransactionRepository.createSwapTransactionForRoute(
            route = activeRoute,
            userPublicKey = tokenKeyProvider.publicKey.toBase58Instance()
        )
        Timber.i("Fresh swap transaction fetched")

        state.value = SwapState.SwapLoaded(
            tokenA = tokenA,
            tokenB = tokenB,
            lamportsTokenA = activeRoute.inAmountInLamports,
            lamportsTokenB = activeRoute.outAmountInLamports,
            routes = updatedRoutes,
            activeRoute = activeRouteIndex,
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
