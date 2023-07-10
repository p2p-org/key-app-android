package org.p2p.wallet.home.events

import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import org.p2p.wallet.bridge.EthereumTokensPollingService
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider

private const val TAG = "EthereumTokensLoader"
class EthereumTokensLoader(
    private val seedPhraseProvider: SeedPhraseProvider,
    private val bridgeFeatureToggle: EthAddressEnabledFeatureToggle,
    private val ethereumInteractor: EthereumInteractor,
    private val ethereumTokensPollingService: EthereumTokensPollingService
) : AppLoader {

    private val isFeatureInitialized: AtomicBoolean = AtomicBoolean(false)

    val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase().seedPhrase

    override suspend fun onLoad() {
        //Initialize ethereum module feature
        if (!isFeatureInitialized.get()) {
            ethereumInteractor.setup(userSeedPhrase = userSeedPhrase)
            isFeatureInitialized.set(true)
        }
        try {
            //Load ethereum tokens
            loadEthereumTokens()
            //Start ethereum tokens polling service
            ethereumTokensPollingService.start()
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Error while loading ethereum tokens")
        } finally {
            //TODO
        }
    }

    override suspend fun isEnabled(): Boolean {
        return userSeedPhrase.isNotEmpty() && bridgeFeatureToggle.isFeatureEnabled
    }

    private suspend fun loadEthereumTokens() {
        val ethereumClaimTokens = ethereumInteractor.loadEthereumClaimTokens()
        val ethereumSendTransactionDetails = ethereumInteractor.loadEthereumSendTransactionDetails()
        ethereumInteractor.loadWalletTokens(ethereumClaimTokens)
    }
}
