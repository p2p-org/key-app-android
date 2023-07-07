package org.p2p.wallet.home.events

import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider

private const val FETCH_INTERVAL_IN_MILLIS = 15_000L

class EthereumKitLoader(
    private val seedPhraseProvider: SeedPhraseProvider,
    private val bridgeFeatureToggle: EthAddressEnabledFeatureToggle,
    private val ethereumInteractor: EthereumInteractor,
    private val appScope: AppScope,
    dispatchers: CoroutineDispatchers,
) : AppLoader, CoroutineScope {

    override val coroutineContext: CoroutineContext = dispatchers.io

    override suspend fun onLoad() {
        val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase().seedPhrase
        if (userSeedPhrase.isNotEmpty() && bridgeFeatureToggle.isFeatureEnabled) {
            ethereumInteractor.setup(userSeedPhrase = userSeedPhrase)
            startTokensPolling()
        } else {
            Timber.w("ETH is not initialized, no seed phrase or disabled")
        }
    }

    private fun startTokensPolling() {
        launch(appScope.coroutineContext) {
            while (isActive) {
                ethereumInteractor.loadWalletTokens()
                delay(FETCH_INTERVAL_IN_MILLIS)
            }
        }
    }
}
