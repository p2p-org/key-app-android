package org.p2p.wallet.home.events

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.wallet.bridge.claim.repository.EthereumBridgeLocalRepository
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails
import org.p2p.wallet.common.feature_toggles.toggles.remote.EthAddressEnabledFeatureToggle
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.transaction.model.progressstate.BridgeSendProgressState
import org.p2p.wallet.transaction.model.progressstate.ClaimProgressState
import org.p2p.wallet.transaction.model.progressstate.TransactionState

class PendingClaimBundlesLoader(
    private val bridgeLocalRepository: EthereumBridgeLocalRepository,
    private val appScope: AppScope,
    private val transactionManager: TransactionManager,
    private val seedPhraseProvider: SeedPhraseProvider,
    private val bridgeFeatureToggle: EthAddressEnabledFeatureToggle
) : AppLoader {

    val userSeedPhrase = seedPhraseProvider.getUserSeedPhrase().seedPhrase

    override suspend fun onLoad() {
        appScope.launch {
            bridgeLocalRepository.observeClaimBundles()
                .combine(bridgeLocalRepository.observeSendDetails()) { claimBundles, sendDetails ->
                    observeClaimBundles(claimBundles)
                    observeSendDetails(sendDetails)
                }
        }
    }

    private suspend fun observeClaimBundles(claimBundles: List<BridgeBundle>) {
        claimBundles.filter(BridgeBundle::isFinalized).forEach { claimBundle ->
            val bundleId = claimBundle.bundleId
            transactionManager.getTransactionStateFlow(bundleId)
                .firstOrNull()
                ?.let { state ->
                    if (state !is TransactionState.Progress) return@let
                    val newState = ClaimProgressState.Success(
                        bundleId = bundleId,
                        sourceTokenSymbol = claimBundle.resultAmount.symbol
                    )
                    transactionManager.emitTransactionState(
                        transactionId = bundleId,
                        state = newState
                    )
                }
        }
    }

    private suspend fun observeSendDetails(sendDetails: List<BridgeSendTransactionDetails>) {
        sendDetails.filter(BridgeSendTransactionDetails::isFinalized).forEach { sendDetail ->
            val bundleId = sendDetail.id
            val newState = BridgeSendProgressState.Success(
                transactionId = bundleId,
                sendDetails = sendDetail
            )
            transactionManager.emitTransactionState(
                transactionId = bundleId,
                state = newState
            )
        }
    }

    override suspend fun isEnabled(): Boolean {
        return userSeedPhrase.isNotEmpty() && bridgeFeatureToggle.isFeatureEnabled
    }
}
