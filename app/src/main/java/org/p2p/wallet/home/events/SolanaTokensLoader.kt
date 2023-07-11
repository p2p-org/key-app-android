package org.p2p.wallet.home.events

import timber.log.Timber
import java.net.UnknownHostException
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.token.service.api.events.manager.TokenServiceEvent
import org.p2p.token.service.api.events.manager.TokenServiceEventManager
import org.p2p.token.service.api.events.manager.TokenServiceEventSubscriber
import org.p2p.token.service.api.events.manager.TokenServiceEventType
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.tokenservice.TokenLoadState
import org.p2p.wallet.user.interactor.UserTokensInteractor
import org.p2p.wallet.utils.toPublicKey

class SolanaTokensLoader(
    private val userTokensInteractor: UserTokensInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val tokenServiceEventManager: TokenServiceEventManager,
    private val appScope: AppScope
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = appScope.coroutineContext
    private val loadState = AtomicReference<TokenLoadState>(TokenLoadState.IDLE)

    suspend fun onStart() {
        try {
            // Subscribe for solana tokens rate updates
            tokenServiceEventManager.subscribe(SolanaTokensRatesEventSubscriber(::saveTokensRates))
            // First step, upload solana tokens from server
            loadState.compareAndSet(null, TokenLoadState.LOADING)

            val tokens = userTokensInteractor.loadUserTokens(tokenKeyProvider.publicKey.toPublicKey())
            userTokensInteractor.saveUserTokens(tokens)
            userTokensInteractor.loadUserRates(tokens)

            loadState.compareAndSet(TokenLoadState.LOADING, TokenLoadState.LOADED)
        } catch (e: CancellationException) {
            Timber.d("Loading sol tokens job cancelled")
        } catch (e: UnknownHostException) {
            Timber.d("Cannot load sol tokens: no internet")
        } catch (t: Throwable) {
            Timber.e(t, "Error on loading sol tokens")
        }
    }

    suspend fun onRefresh() {
        try {
            loadState.compareAndSet(TokenLoadState.LOADED, TokenLoadState.REFRESHING)

            val tokens = userTokensInteractor.loadUserTokens(tokenKeyProvider.publicKey.toPublicKey())
            userTokensInteractor.saveUserTokens(tokens)
            userTokensInteractor.loadUserRates(tokens)

            loadState.compareAndSet(TokenLoadState.REFRESHING, TokenLoadState.LOADED)
        } catch (e: CancellationException) {
            Timber.d("Loading sol tokens job cancelled")
        } catch (e: UnknownHostException) {
            Timber.d("Cannot load sol tokens: no internet")
        } catch (t: Throwable) {
            Timber.e(t, "Error on loading sol tokens")
        }
    }

    fun getCurrentLoadState(): TokenLoadState {
        return loadState.get()
    }

    private fun saveTokensRates(list: List<TokenServicePrice>) {
        launch {
            userTokensInteractor.saveUserTokensRates(list)
        }
    }

    private inner class SolanaTokensRatesEventSubscriber(private val block: (List<TokenServicePrice>) -> Unit) :
        TokenServiceEventSubscriber {

        override fun onUpdate(eventType: TokenServiceEventType, event: TokenServiceEvent) {
            if (eventType != TokenServiceEventType.SOLANA_CHAIN_EVENT) return
            when (event) {
                is TokenServiceEvent.Loading -> Unit
                is TokenServiceEvent.TokensPriceLoaded -> block(event.result)
                is TokenServiceEvent.Idle -> Unit
                else -> Unit
            }
        }
    }
}
