package org.p2p.wallet.swap.jupiter.statemanager.rate

import java.math.BigDecimal
import java.util.concurrent.Executors
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.p2p.core.utils.Constants.USD_READABLE_SYMBOL
import org.p2p.core.utils.scaleLong
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.jupiter.statemanager.SwapCoroutineScope
import org.p2p.wallet.swap.jupiter.statemanager.SwapState
import org.p2p.wallet.swap.model.jupiter.SwapRateTickerState
import org.p2p.wallet.user.repository.UserLocalRepository
import org.p2p.wallet.user.repository.prices.TokenId
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository

class SwapRateTickerManager private constructor(
    swapScope: SwapCoroutineScope,
    private val userLocalRepository: UserLocalRepository,
    private val tokenPricesRepository: TokenPricesRemoteRepository,
    private val initDispatcher: CoroutineDispatcher
) : CoroutineScope by (swapScope + initDispatcher) {

    constructor(
        swapScope: SwapCoroutineScope,
        userLocalRepository: UserLocalRepository,
        tokenPricesRepository: TokenPricesRemoteRepository
    ) : this(
        swapScope = swapScope,
        userLocalRepository = userLocalRepository,
        tokenPricesRepository = tokenPricesRepository,
        initDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    )

    private var currentTokenA: SwapTokenModel? = null
    private var currentTokenB: SwapTokenModel? = null

    private val currentRateState = MutableStateFlow<SwapRateTickerState>(SwapRateTickerState.Loading)

    fun observe(): Flow<SwapRateTickerState> = currentRateState

    fun handleRoutesLoading(state: SwapState.LoadingRoutes) {
        currentRateState.value = SwapRateTickerState.Loading
    }

    fun handleSwapException(state: SwapState.SwapException) {
        currentRateState.value = SwapRateTickerState.Hidden
    }

    fun handleJupiterRates(state: SwapState.SwapLoaded) {
        val newTokenA = state.tokenA.also { currentTokenA = it }
        val newTokenB = state.tokenB.also { currentTokenB = it }

        val result = when {
            !newTokenA.isStableCoin() && newTokenB.isStableCoin() -> {
                val newRate = (state.amountTokenB / state.amountTokenA).scaleLong(newTokenB.decimals)
                "1 ${newTokenA.tokenSymbol} ≈ $newRate ${newTokenB.tokenSymbol}"
            }
            else -> {
                val newRate = (state.amountTokenA / state.amountTokenB).scaleLong(newTokenA.decimals)
                "1 ${newTokenB.tokenSymbol} ≈ $newRate ${newTokenA.tokenSymbol}"
            }
        }

        currentRateState.value = SwapRateTickerState.Shown(result)
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
        val (from, to) = when {
            !tokenA.isStableCoin() && tokenB.isStableCoin() -> tokenA to tokenB
            else -> tokenB to tokenA
        }

        val amountFrom = when (from) {
            is SwapTokenModel.UserToken -> from.details.rate
            is SwapTokenModel.JupiterToken -> findJupiterTokenRate(from)
        } ?: return SwapRateTickerState.Hidden

        val amountTo = when (to) {
            is SwapTokenModel.UserToken -> to.details.rate
            is SwapTokenModel.JupiterToken -> findJupiterTokenRate(to)
        } ?: return SwapRateTickerState.Hidden

        val newRate = (amountFrom / amountTo).scaleLong(to.decimals)

        val result = "1 ${from.tokenSymbol} ≈ $newRate ${to.tokenSymbol}"
        return SwapRateTickerState.Shown(result)
    }

    private suspend fun findJupiterTokenRate(to: SwapTokenModel): BigDecimal? {
        val tokenData = userLocalRepository.findTokenData(to.mintAddress.base58Value)
        val coingeckoId = tokenData?.coingeckoId ?: return null
        val price = userLocalRepository.getPriceByTokenId(coingeckoId)
        return price?.price ?: tokenPricesRepository.getTokenPriceById(TokenId(coingeckoId), USD_READABLE_SYMBOL).price
    }

    fun stopAll() {
        coroutineContext.cancelChildren()
    }
}
