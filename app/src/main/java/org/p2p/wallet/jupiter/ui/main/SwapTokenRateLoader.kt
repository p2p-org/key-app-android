package org.p2p.wallet.jupiter.ui.main

import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import org.p2p.token.service.repository.TokenServiceRepository
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel

class SwapTokenRateLoader(
    private val tokenServiceRepository: TokenServiceRepository
) {
    private val state = AtomicReference<SwapRateLoaderState>(SwapRateLoaderState.Empty)

    fun getRate(token: SwapTokenModel): Flow<SwapRateLoaderState> = flow {
        val oldState = state.get()
        val oldLoadedToken: SwapTokenModel? = oldState.getOldToken()

        val rateNotLoaded =
            oldLoadedToken == null
        val isRateForOtherToken =
            oldLoadedToken?.mintAddress != token.mintAddress || oldLoadedToken::class != token::class
        val isRateLoaded =
            oldLoadedToken?.mintAddress == token.mintAddress

        when {
            rateNotLoaded || isRateForOtherToken -> updateToken(token)
            isRateLoaded -> emitAndSaveState(oldState)
            else -> updateToken(token)
        }
    }

    private suspend fun FlowCollector<SwapRateLoaderState>.updateToken(newToken: SwapTokenModel) {
        return when (newToken) {
            is SwapTokenModel.JupiterToken -> updateJupiterTokenRate(newToken)
            is SwapTokenModel.UserToken -> updateUserTokenRate(newToken)
        }
    }

    private suspend fun FlowCollector<SwapRateLoaderState>.updateUserTokenRate(
        token: SwapTokenModel.UserToken
    ) {
        val rate = token.details.rate
        val newState = if (rate != null) {
            SwapRateLoaderState.Loaded(token = token, rate = rate)
        } else {
            SwapRateLoaderState.NoRateAvailable(token = token)
        }
        emitAndSaveState(newState)
    }

    private suspend fun FlowCollector<SwapRateLoaderState>.updateJupiterTokenRate(
        token: SwapTokenModel.JupiterToken
    ) {
        emitAndSaveState(SwapRateLoaderState.Loading)
        try {
            val tokenPrice =
                tokenServiceRepository.findTokenPriceByAddress(
                    tokenAddress = token.details.tokenMint.base58Value
                )?.getUsdRate()
            if (tokenPrice != null) {
                emitAndSaveState(SwapRateLoaderState.Loaded(token = token, rate = tokenPrice))
            } else {
                emitAndSaveState(SwapRateLoaderState.NoRateAvailable(token = token))
            }
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
