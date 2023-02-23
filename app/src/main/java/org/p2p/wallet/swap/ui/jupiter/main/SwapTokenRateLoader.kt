package org.p2p.wallet.swap.ui.jupiter.main

import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import org.p2p.core.utils.Constants
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.user.repository.prices.TokenId
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository

class SwapTokenRateLoader(
    private val tokenPricesRepository: TokenPricesRemoteRepository,
) {
    private val state = AtomicReference<SwapRateLoaderState>(SwapRateLoaderState.Empty)

    fun getRate(token: SwapTokenModel): Flow<SwapRateLoaderState> = flow {
        val oldState = state.get()
        val oldLoadedToken: SwapTokenModel? = oldState.getOldToken()

        when {
            oldLoadedToken == null ||
                oldLoadedToken.mintAddress != token.mintAddress ||
                oldLoadedToken::class != token::class -> updateToken(token)
            oldLoadedToken.mintAddress == token.mintAddress -> emitAndSaveState(oldState)
            else -> updateToken(token)
        }
    }

    private suspend fun FlowCollector<SwapRateLoaderState>.updateToken(newToken: SwapTokenModel) {
        return when (newToken) {
            is SwapTokenModel.JupiterToken -> updateJupiterTokenRate(newToken)
            is SwapTokenModel.UserToken -> updateUserTokenRate(newToken)
        }
    }

    private suspend fun FlowCollector<SwapRateLoaderState>.updateUserTokenRate(token: SwapTokenModel.UserToken) {
        val rate = token.details.rate
        val newState = if (rate != null) {
            SwapRateLoaderState.Loaded(token = token, rate = rate)
        } else {
            SwapRateLoaderState.NoRateAvailable(token = token)
        }
        emitAndSaveState(newState)
    }

    private suspend fun FlowCollector<SwapRateLoaderState>.updateJupiterTokenRate(token: SwapTokenModel.JupiterToken) {
        val coingeckoId = token.coingeckoId
        if (coingeckoId == null) {
            emitAndSaveState(SwapRateLoaderState.NoRateAvailable(token))
            return
        }

        emitAndSaveState(SwapRateLoaderState.Loading)
        try {
            val tokenPrice = tokenPricesRepository.getTokenPriceById(
                tokenId = TokenId(coingeckoId),
                targetCurrency = Constants.USD_READABLE_SYMBOL
            )
            emitAndSaveState(SwapRateLoaderState.Loaded(token = token, rate = tokenPrice.price))
        } catch (e: CancellationException) {
            Timber.i(e)
        } catch (e: Throwable) {
            emitAndSaveState(SwapRateLoaderState.Error)
        }
    }

    private fun SwapRateLoaderState.getOldToken(): SwapTokenModel? = when (this) {
        SwapRateLoaderState.Error,
        SwapRateLoaderState.Loading,
        SwapRateLoaderState.Empty -> null
        is SwapRateLoaderState.NoRateAvailable -> this.token
        is SwapRateLoaderState.Loaded -> this.token
    }

    private suspend fun FlowCollector<SwapRateLoaderState>.emitAndSaveState(newState: SwapRateLoaderState) {
        state.set(newState)
        emit(newState)
    }
}
