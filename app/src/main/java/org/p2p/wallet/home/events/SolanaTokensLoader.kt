package org.p2p.wallet.home.events

import timber.log.Timber
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.p2p.core.common.di.AppScope
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.core.network.ConnectionManager
import org.p2p.core.network.environment.NetworkEnvironmentManager
import org.p2p.token.service.api.events.manager.TokenServiceEvent
import org.p2p.token.service.api.events.manager.TokenServiceEventManager
import org.p2p.token.service.api.events.manager.TokenServiceEventSubscriber
import org.p2p.token.service.api.events.manager.TokenServiceEventType
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.wallet.home.ui.main.HomeInteractor
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.user.interactor.UserTokensInteractor
import org.p2p.wallet.utils.toPublicKey

class SolanaTokensLoader(
    private val userTokensInteractor: UserTokensInteractor,
    private val tokenKeyProvider: TokenKeyProvider,
    private val tokenServiceEventManager: TokenServiceEventManager,
    private val dispatchers: CoroutineDispatchers
) : AppLoader, CoroutineScope {

    override val coroutineContext: CoroutineContext = dispatchers.io

    override suspend fun onLoad() {
        try {
            //Subscribe for solana tokens rate updates
            tokenServiceEventManager.subscribe(SolanaTokensRatesEventSubscriber(::saveTokensRates))

            //First step, upload solana tokens from server
            val tokens = userTokensInteractor.loadUserTokens(
                tokenKeyProvider.publicKey.toPublicKey()
            )
            //Second step, save them in database
            userTokensInteractor.saveUserTokens(tokens)

            //Third step, request tokens rate for user tokens
            userTokensInteractor.loadUserRates(tokens)
        } catch (e: CancellationException) {
            Timber.d("Loading sol tokens job cancelled")
        } catch (e: UnknownHostException) {
            Timber.d("Cannot load sol tokens: no internet")
        } catch (t: Throwable) {
            Timber.e(t, "Error on loading sol tokens")
        } finally {
        }
    }

    override suspend fun onRefresh() {
        onLoad()
    }

    override suspend fun isEnabled(): Boolean {
        return true
    }

    private fun saveTokensRates(list: List<TokenServicePrice>) {
        launch {
            Timber.tag("_______").d("Prices received = ${list.size}")
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
