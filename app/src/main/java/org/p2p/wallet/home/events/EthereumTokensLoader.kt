package org.p2p.wallet.home.events

import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import org.p2p.wallet.tokenservice.model.EthTokenLoadState

private const val TAG = "EthereumTokensLoader"

class EthereumTokensLoader(
    seedPhraseProvider: SeedPhraseProvider,
    private val bridgeFeatureToggle: EthAddressEnabledFeatureToggle,
    private val ethereumInteractor: EthereumInteractor,
    private val ethereumTokensPollingService: EthereumTokensPollingService,
    private val tokenServiceEventPublisher: TokenServiceEventPublisher,
    private val tokenServiceEventManager: TokenServiceEventManager,
    appScope: AppScope
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = appScope.coroutineContext
    private val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase().seedPhrase

    private val state: MutableStateFlow<EthTokenLoadState> = MutableStateFlow(EthTokenLoadState.Idle)

    fun observeState(): Flow<EthTokenLoadState> = state.asStateFlow()

    suspend fun loadIfEnabled() {
        if (!isEnabled()) return

        try {
            updateState(EthTokenLoadState.Loading)
            ethereumInteractor.setup(userSeedPhrase = userSeedPhrase)
            tokenServiceEventManager.subscribe(EthereumTokensRatesEventSubscriber(::saveTokensRates))

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
            updateState(EthTokenLoadState.Error(e))
        }
    }

    suspend fun refreshIfEnabled() {
        if (!isEnabled()) return

        try {
            updateState(EthTokenLoadState.Refreshing)
            val claimTokens = ethereumInteractor.loadClaimTokens()

            ethereumInteractor.loadSendTransactionDetails()
            val ethTokens = ethereumInteractor.loadWalletTokens(claimTokens)

            ethereumInteractor.cacheWalletTokens(ethTokens)
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Error while refreshing ethereum tokens")
            updateState(EthTokenLoadState.Error(e))
        }
    }

    private fun isEnabled(): Boolean {
        return userSeedPhrase.isNotEmpty() && bridgeFeatureToggle.isFeatureEnabled
    }

    private fun saveTokensRates(list: List<TokenServicePrice>) {
        launch {
            ethereumInteractor.updateTokensRates(list)
        }
    }

    private inner class EthereumTokensRatesEventSubscriber(
        private val block: (List<TokenServicePrice>) -> Unit
    ) : TokenServiceEventSubscriber {

        override fun onUpdate(eventType: TokenServiceEventType, event: TokenServiceEvent) {
            if (eventType != TokenServiceEventType.ETHEREUM_CHAIN_EVENT) return

            when (event) {
                is TokenServiceEvent.Loading -> Unit
                is TokenServiceEvent.TokensPriceLoaded -> block(event.result)
                is TokenServiceEvent.Idle -> Unit
                else -> Unit
            }
        }
    }

    private fun updateState(newState: EthTokenLoadState) {
        state.value = newState
    }
}
