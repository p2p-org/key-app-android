package org.p2p.wallet.jupiter.statemanager.rate

import timber.log.Timber
import java.math.BigDecimal
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.p2p.core.utils.formatToken
import org.p2p.token.service.repository.TokenServiceRepository
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.model.SwapRateTickerState
import org.p2p.wallet.jupiter.statemanager.SwapCoroutineScope
import org.p2p.wallet.jupiter.statemanager.SwapState
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.utils.divideSafe

private const val TAG = "SwapRateTickerManager"

class SwapRateTickerManager(
    swapScope: SwapCoroutineScope,
    private val userLocalRepository: UserLocalRepository,
    private val tokenServiceRepository: TokenServiceRepository,
    private val initDispatcher: CoroutineDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
) : CoroutineScope by (swapScope + initDispatcher) {

    private var currentTokenA: SwapTokenModel? = null
    private var currentTokenB: SwapTokenModel? = null

    private val currentRateState = MutableStateFlow<SwapRateTickerState>(SwapRateTickerState.Loading)

    fun observe(): Flow<SwapRateTickerState> =
        currentRateState
            .onEach { Timber.tag(TAG).i("SwapRateTickerState changed: $it") }

    fun handleRoutesLoading(state: SwapState.LoadingRoutes) {
        currentRateState.value = SwapRateTickerState.Loading
    }

    fun handleSwapException(state: SwapState.SwapException) {
        currentRateState.value = SwapRateTickerState.Hidden
    }

    fun handleJupiterRates(state: SwapState.SwapLoaded) {
        val newTokenA = state.tokenA.also { currentTokenA = it }
        val newTokenB = state.tokenB.also { currentTokenB = it }

        val rateText = when {
            !newTokenA.isStableCoin() && newTokenB.isStableCoin() -> {
                val newRate = state.amountTokenB.divideSafe(state.amountTokenA).formatToken(newTokenB.decimals)
                formatRateString(newTokenA.tokenSymbol, newRate, newTokenB.tokenSymbol)
            }
            else -> {
                val newRate = state.amountTokenA.divideSafe(state.amountTokenB)
                formatRateString(newTokenB.tokenSymbol, newRate.formatToken(newTokenA.decimals), newTokenA.tokenSymbol)
            }
        }

        currentRateState.value = SwapRateTickerState.Shown(rateText)
    }

    fun onInitialTokensSelected(
        newTokenA: SwapTokenModel?,
        newTokenB: SwapTokenModel?
    ) {
        if (newTokenA == null || newTokenB == null) return

        val currentTokenAMint = currentTokenA?.mintAddress
        val currentTokenBMint = currentTokenB?.mintAddress
        if (newTokenA.mintAddress == currentTokenAMint && newTokenB.mintAddress == currentTokenBMint) {
            return
        }

        currentTokenA = newTokenA
        currentTokenB = newTokenB

        launch {
            currentRateState.value = SwapRateTickerState.Loading

            val newRatesState = findTokensRatesState(newTokenA, newTokenB)
            currentRateState.value = newRatesState
        }
    }

    /**
     * Finds token price ratio to each other. Returns the formatted string
     * @sample 1 ETH (Token B) = 49 SOL (Token A)
     *
     * If Token B is Stable, then showing the formatting in this way
     * @sample 1 SOL (Token A) = 28 USDC (Token B)
     *
     * */
    private suspend fun findTokensRatesState(tokenA: SwapTokenModel, tokenB: SwapTokenModel): SwapRateTickerState {
        val (from, to) = if (!tokenA.isStableCoin() && tokenB.isStableCoin()) {
            tokenA to tokenB
        } else {
            tokenB to tokenA
        }

        val amountFrom = when (from) {
            is SwapTokenModel.UserToken -> from.details.rate
            is SwapTokenModel.JupiterToken -> findJupiterTokenRate(from)
        } ?: return SwapRateTickerState.Hidden

        val amountTo = when (to) {
            is SwapTokenModel.UserToken -> to.details.rate
            is SwapTokenModel.JupiterToken -> findJupiterTokenRate(to)
        } ?: return SwapRateTickerState.Hidden

        val newRate = (amountFrom / amountTo).formatToken(to.decimals)

        return SwapRateTickerState.Shown(formatRateString(from.tokenSymbol, newRate, to.tokenSymbol))
    }

    private suspend fun findJupiterTokenRate(to: SwapTokenModel.JupiterToken): BigDecimal? {
        val tokenData = userLocalRepository.findTokenData(to.mintAddress.base58Value) ?: return null
        val cachedPrice = tokenServiceRepository.findTokenPriceByAddress(
            tokenAddress = tokenData.mintAddress
        )
        return cachedPrice?.getUsdRate()
    }

    private fun formatRateString(tokenASymbol: String, tokenBRate: String, tokenBSymbol: String): String {
        return "1 $tokenASymbol ≈ $tokenBRate $tokenBSymbol"
    }

    fun stopAll() {
        coroutineContext.cancelChildren()
    }
}
