package org.p2p.wallet.home.events

import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.token.service.api.events.manager.TokenServiceEvent
import org.p2p.token.service.api.events.manager.TokenServiceEventManager
import org.p2p.token.service.api.events.manager.TokenServiceEventPublisher
import org.p2p.token.service.api.events.manager.TokenServiceEventSubscriber
import org.p2p.token.service.api.events.manager.TokenServiceEventType
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.wallet.bridge.EthereumTokensPollingService
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.tokenservice.TokenLoadState

private const val TAG = "EthereumTokensLoader"

class EthereumTokensLoader(
    private val seedPhraseProvider: SeedPhraseProvider,
    private val bridgeFeatureToggle: EthAddressEnabledFeatureToggle,
    private val ethereumInteractor: EthereumInteractor,
    private val ethereumTokensPollingService: EthereumTokensPollingService,
    private val tokenServiceEventPublisher: TokenServiceEventPublisher,
    private val tokenServiceEventManager: TokenServiceEventManager,
    appScope: AppScope
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = appScope.coroutineContext
    private val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase().seedPhrase
    private val loadState = AtomicReference(TokenLoadState.IDLE)

    suspend fun onStart() {
        try {
            ethereumInteractor.setup(userSeedPhrase = userSeedPhrase)
            tokenServiceEventManager.subscribe(EthereumTokensRatesEventSubscriber(::saveTokensRates))

            loadState.compareAndSet(TokenLoadState.IDLE, TokenLoadState.LOADING)
            val claimTokens = ethereumInteractor.loadClaimTokens()
            ethereumInteractor.loadSendTransactionDetails()

            val ethTokens = ethereumInteractor.loadWalletTokens(claimTokens)

            ethereumInteractor.cacheWalletTokens(ethTokens)
            tokenServiceEventPublisher.loadTokensPrice(
                networkChain = TokenServiceNetwork.ETHEREUM,
                addresses = ethTokens.map { it.publicKey }
            )
            ethereumTokensPollingService.start()
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Error while loading ethereum tokens")
        } finally {
            loadState.compareAndSet(TokenLoadState.LOADING, TokenLoadState.LOADED)
        }
    }

    suspend fun onRefresh() {
        try {
            loadState.compareAndSet(TokenLoadState.LOADED, TokenLoadState.REFRESHING)
            val claimTokens = ethereumInteractor.loadClaimTokens()

            ethereumInteractor.loadSendTransactionDetails()
            val ethTokens = ethereumInteractor.loadWalletTokens(claimTokens)

            ethereumInteractor.cacheWalletTokens(ethTokens)
            loadState.compareAndSet(TokenLoadState.REFRESHING, TokenLoadState.LOADED)
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Error while refreshing ethereum tokens")
        }
    }

    fun getCurrentLoadState(): TokenLoadState {
        return loadState.get()
    }

    fun isEnabled(): Boolean {
        return userSeedPhrase.isNotEmpty() && bridgeFeatureToggle.isFeatureEnabled
    }

    private fun saveTokensRates(list: List<TokenServicePrice>) {
        launch {
            ethereumInteractor.updateTokensRates(list)
        }
    }

    private inner class EthereumTokensRatesEventSubscriber(private val block: (List<TokenServicePrice>) -> Unit) :
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
