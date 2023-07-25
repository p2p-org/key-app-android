package org.p2p.wallet.home.events

import timber.log.Timber
import java.math.BigDecimal
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.core.token.Token
import org.p2p.core.utils.orZero
import org.p2p.token.service.api.events.manager.TokenServiceEventManager
import org.p2p.token.service.api.events.manager.TokenServiceEventPublisher
import org.p2p.token.service.api.events.manager.TokenServiceEventSubscriber
import org.p2p.token.service.api.events.manager.TokenServiceEventType
import org.p2p.token.service.api.events.manager.TokenServiceUpdate
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.tokenservice.model.EthTokenLoadState

private const val TAG = "EthereumTokensLoader"
private val MINIMAL_DUST = BigDecimal("5")

class EthereumTokensLoader(
    private val seedPhraseProvider: SeedPhraseProvider,
    private val bridgeFeatureToggle: EthAddressEnabledFeatureToggle,
    private val ethereumInteractor: EthereumInteractor,
    private val tokenServiceEventPublisher: TokenServiceEventPublisher,
    private val tokenServiceEventManager: TokenServiceEventManager,
    appScope: AppScope
) : CoroutineScope {

    private val state: MutableStateFlow<EthTokenLoadState> = MutableStateFlow(EthTokenLoadState.Idle)

    // Caching last ethereum tokens, to prevent hiding tokens while refreshing
    private val ethereumTokens = mutableListOf<Token.Eth>()

    init {
        ethereumInteractor.observeTokensFlow()
            .onEach { ethTokens ->
                val filteredEthTokens = ethTokens.filter { token ->
                    val tokenFiatAmount = token.totalInUsd.orZero()
                    val isClaimInProgress = token.isClaiming
                    val isFiatAmountAboveThreshold = tokenFiatAmount >= MINIMAL_DUST
                    isFiatAmountAboveThreshold || isClaimInProgress
                }
                ethereumTokens.clear()
                ethereumTokens.addAll(filteredEthTokens)
                updateState(EthTokenLoadState.Loaded(filteredEthTokens))
            }
            .launchIn(appScope)
    }

    override val coroutineContext: CoroutineContext = appScope.coroutineContext

    fun observeState(): Flow<EthTokenLoadState> = state.asStateFlow()

    fun getTokens(): List<Token.Eth> = ethereumTokens

    suspend fun loadIfEnabled() {
        if (!isEnabled()) {
            updateState(EthTokenLoadState.Loaded(emptyList()))
            return
        }

        try {
            setupEthereum()
            updateState(EthTokenLoadState.Loading)
            ethereumInteractor.setup(userSeedPhrase = seedPhraseProvider.getUserSeedPhrase().seedPhrase)
            tokenServiceEventManager.subscribe(EthereumTokensRatesEventSubscriber(::saveTokensRates))

            val claimTokens = ethereumInteractor.loadClaimTokens()
            ethereumInteractor.loadSendTransactionDetails()

            val ethTokens = ethereumInteractor.loadWalletTokens(claimTokens)
            ethereumInteractor.cacheWalletTokens(ethTokens)
            tokenServiceEventPublisher.loadTokensPrice(
                networkChain = TokenServiceNetwork.ETHEREUM,
                addresses = ethTokens.map { it.tokenServiceAddress }
            )
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Error while loading ethereum tokens")
            updateState(EthTokenLoadState.Error(e))
        }
    }

    suspend fun refreshIfEnabled() {
        if (!isEnabled()) {
            updateState(EthTokenLoadState.Loaded(emptyList()))
            return
        }

        try {
            setupEthereum()
            updateState(EthTokenLoadState.Refreshing)
            val claimTokens = ethereumInteractor.loadClaimTokens()

            ethereumInteractor.loadSendTransactionDetails()
            val ethTokens = ethereumInteractor.loadWalletTokens(claimTokens)

            ethereumInteractor.cacheWalletTokens(ethTokens)
            tokenServiceEventPublisher.loadTokensPrice(
                networkChain = TokenServiceNetwork.ETHEREUM,
                addresses = ethTokens.map { it.tokenServiceAddress }
            )
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Error while refreshing ethereum tokens")
            updateState(EthTokenLoadState.Error(e))
        }
    }

    private fun isEnabled(): Boolean {
        return seedPhraseProvider.isAvailable && bridgeFeatureToggle.isFeatureEnabled
    }

    private fun setupEthereum() {
        if (!ethereumInteractor.isInitialized()) {
            ethereumInteractor.setup(
                userSeedPhrase = seedPhraseProvider.getUserSeedPhrase().seedPhrase
            )
        }
    }

    private fun saveTokensRates(list: List<TokenServicePrice>) {
        launch {
            ethereumInteractor.updateTokensRates(list)
        }
    }

    private inner class EthereumTokensRatesEventSubscriber(
        private val block: (List<TokenServicePrice>) -> Unit
    ) : TokenServiceEventSubscriber {

        override fun onUpdate(eventType: TokenServiceEventType, data: TokenServiceUpdate) {
            if (eventType != TokenServiceEventType.ETHEREUM_CHAIN_EVENT) return
            when (data) {
                is TokenServiceUpdate.Loading -> Unit
                is TokenServiceUpdate.TokensPriceLoaded -> block(data.result)
                is TokenServiceUpdate.Idle -> Unit
                else -> Unit
            }
        }
    }

    private fun updateState(newState: EthTokenLoadState) {
        state.value = newState
    }
}
