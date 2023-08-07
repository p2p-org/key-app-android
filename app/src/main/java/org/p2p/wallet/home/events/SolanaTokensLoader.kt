package org.p2p.wallet.home.events

import timber.log.Timber
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.core.token.Token
import org.p2p.core.token.filterTokensByAvailability
import org.p2p.token.service.api.events.manager.TokenServiceEventManager
import org.p2p.token.service.api.events.manager.TokenServiceEventSubscriber
import org.p2p.token.service.api.events.manager.TokenServiceEventType
import org.p2p.token.service.api.events.manager.TokenServiceUpdate
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.tokenservice.model.SolanaTokenLoadState
import org.p2p.wallet.user.interactor.UserTokensInteractor
import org.p2p.wallet.utils.toPublicKey

class SolanaTokensLoader(
    private val userTokensInteractor: UserTokensInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val tokenServiceEventManager: TokenServiceEventManager,
    private val appScope: AppScope
) {

    private val state: MutableStateFlow<SolanaTokenLoadState> = MutableStateFlow(SolanaTokenLoadState.Idle)

    init {
        userTokensInteractor.observeUserTokens()
            .onEach { updateState(SolanaTokenLoadState.Loaded(it.filterTokensByAvailability())) }
            .launchIn(appScope)
    }

    fun observeState(): Flow<SolanaTokenLoadState> = state

    suspend fun load() {
        try {
            updateState(SolanaTokenLoadState.Loading)

            tokenServiceEventManager.subscribe(SolanaTokensRatesEventSubscriber(::saveTokensRates))
            val tokens = userTokensInteractor.loadUserTokens(tokenKeyProvider.publicKey.toPublicKey())
            userTokensInteractor.saveUserTokens(tokens)
            userTokensInteractor.loadUserRates(tokens)
        } catch (e: CancellationException) {
            Timber.d("Loading sol tokens job cancelled")
        } catch (e: Throwable) {
            Timber.e(e, "Error on loading sol tokens")
            updateState(SolanaTokenLoadState.Error(e))
        }
    }

    suspend fun refresh() {
        try {
            updateState(SolanaTokenLoadState.Refreshing)

            val tokens = userTokensInteractor.loadUserTokens(tokenKeyProvider.publicKey.toPublicKey())
            userTokensInteractor.saveUserTokens(tokens)
            userTokensInteractor.loadUserRates(tokens)
        } catch (e: CancellationException) {
            Timber.d("Refreshing sol tokens job cancelled")
        } catch (e: Throwable) {
            Timber.e(e, "Error on refreshing sol tokens")
            updateState(SolanaTokenLoadState.Error(e))
        }
    }

    suspend fun getUserTokens(): List<Token.Active> {
        return userTokensInteractor.getUserTokens().filterTokensByAvailability()
    }

    private fun saveTokensRates(list: List<TokenServicePrice>) {
        appScope.launch {
            userTokensInteractor.saveUserTokensRates(list)
        }
    }

    private fun updateState(newState: SolanaTokenLoadState) {
        state.value = newState
    }

    private inner class SolanaTokensRatesEventSubscriber(
        private val block: (List<TokenServicePrice>) -> Unit
    ) : TokenServiceEventSubscriber {

        override fun onUpdate(eventType: TokenServiceEventType, data: TokenServiceUpdate) {
            if (eventType != TokenServiceEventType.SOLANA_CHAIN_EVENT) return

            when (data) {
                is TokenServiceUpdate.Loading -> Unit
                is TokenServiceUpdate.TokensPriceLoaded -> block.invoke(data.result)
                is TokenServiceUpdate.Idle -> Unit
                else -> Unit
            }
        }
    }
}
