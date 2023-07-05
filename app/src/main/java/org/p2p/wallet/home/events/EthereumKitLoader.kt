package org.p2p.wallet.home.events

import android.content.Context
import timber.log.Timber
import org.p2p.wallet.bridge.interactor.EthereumInteractor
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.user.worker.PendingTransactionMergeWorker

class EthereumKitLoader(
    private val seedPhraseProvider: SeedPhraseProvider,
    private val bridgeFeatureToggle: EthAddressEnabledFeatureToggle,
    private val ethereumInteractor: EthereumInteractor,
    private val context: Context,
) : HomeScreenLoader {

    override suspend fun onLoad() {
        val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase().seedPhrase
        if (userSeedPhrase.isNotEmpty() && bridgeFeatureToggle.isFeatureEnabled) {
            ethereumInteractor.setup(userSeedPhrase = userSeedPhrase)
            PendingTransactionMergeWorker.scheduleWorker(context)
        } else {
            Timber.w("ETH is not initialized, no seed phrase or disabled")
        }
    }

    override suspend fun onRefresh() = Unit
}
