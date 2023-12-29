package org.p2p.wallet.jupiter.statemanager

import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.core.crypto.toBase58Instance
import org.p2p.core.utils.fromLamports
import org.p2p.core.utils.toLamports
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.repository.model.JupiterSwapPair
import org.p2p.wallet.jupiter.repository.model.JupiterSwapRoute
import org.p2p.wallet.jupiter.repository.model.SwapFailure
import org.p2p.wallet.jupiter.repository.routes.JupiterSwapRoutesRepository
import org.p2p.wallet.jupiter.repository.transaction.JupiterSwapTransactionRepository
import org.p2p.wallet.jupiter.statemanager.validator.MinimumSolAmountValidator
import org.p2p.wallet.jupiter.statemanager.validator.SwapValidator
import org.p2p.wallet.swap.model.Slippage

class SwapStateRoutesRefresher(
    private val tokenKeyProvider: TokenKeyProvider,
    private val swapRoutesRepository: JupiterSwapRoutesRepository,
    private val swapTransactionRepository: JupiterSwapTransactionRepository,
    private val minSolBalanceValidator: MinimumSolAmountValidator,
    private val swapValidator: SwapValidator,
    private val swapProfiler: SwapProfiler,
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
        swapValidator.validateIsSameTokens(tokenA = tokenA, tokenB = tokenB)
        state.value = SwapState.LoadingRoutes(
            tokenA = tokenA,
            tokenB = tokenB,
            amountTokenA = amountTokenA,
            slippage = slippage
        )

        val updatedRoutes = try {
            fetchRoutes(
                tokenA = tokenA,
                tokenB = tokenB,
                amountTokenA = amountTokenA,
                slippage = slippage
            )
        } catch (e: SwapFailure.TooSmallInputAmount) {
            throw SwapFeatureException.SmallTokenAAmount(amountTokenA)
        } finally {
            swapProfiler.setRoutesFetchedTime()
        }

        Timber.i("Jupiter routes fetched: ${updatedRoutes.size}")

        val activeRoute = updatedRoutes.getOrNull(activeRouteIndex)
            ?: throw SwapFeatureException.RoutesNotFound

        val amountTokenB = activeRoute
            .outAmountInLamports
            .fromLamports(tokenB.decimals)

        try {
            swapValidator.validateInputAmount(tokenA = tokenA, amountTokenA = amountTokenA)
        } catch (notValidTokenA: SwapFeatureException.NotValidTokenA) {
            state.value = SwapState.RoutesLoaded(
                tokenA = tokenA,
                tokenB = tokenB,
                amountTokenA = amountTokenA,
                routes = updatedRoutes,
                activeRouteIndex = activeRouteIndex,
                amountTokenB = amountTokenB,
                slippage = slippage,
            )
            throw notValidTokenA
        }

        state.value = SwapState.LoadingTransaction(
            tokenA = tokenA,
            tokenB = tokenB,
            amountTokenA = amountTokenA,
            routes = updatedRoutes,
            activeRouteIndex = activeRouteIndex,
            amountTokenB = amountTokenB,
            slippage = slippage,
        )

        val freshSwapTransaction = swapTransactionRepository.createSwapTransactionForRoute(
            route = activeRoute,
            userPublicKey = tokenKeyProvider.publicKey.toBase58Instance()
        )
        swapProfiler.setTxCreatedTime()

        Timber.i("Fresh swap transaction fetched")

        state.value = SwapState.SwapLoaded(
            tokenA = tokenA,
            tokenB = tokenB,
            lamportsTokenA = activeRoute.inAmountInLamports,
            lamportsTokenB = activeRoute.outAmountInLamports,
            routes = updatedRoutes,
            activeRouteIndex = activeRouteIndex,
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
        val validateRoutes = swapValidator.isValidInputAmount(tokenA, amountTokenA)
        return swapRoutesRepository.getSwapRoutesForSwapPair(
            jupiterSwapPair = routesRequest,
            userPublicKey = tokenKeyProvider.publicKey.toBase58Instance(),
            // don't validate routes if amount is invalid or balance insufficient
            validateRoutes = validateRoutes
        )
    }
}
