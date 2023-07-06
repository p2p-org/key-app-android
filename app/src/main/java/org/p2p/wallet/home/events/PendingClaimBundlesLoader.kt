package org.p2p.wallet.home.events

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.p2p.core.common.di.AppScope
import org.p2p.wallet.bridge.claim.repository.EthereumBridgeLocalRepository
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.transaction.model.TransactionState

class PendingClaimBundlesLoader(
    private val bridgeLocalRepository: EthereumBridgeLocalRepository,
    private val appScope: AppScope,
    private val transactionManager: TransactionManager
) : HomeScreenLoader {

    override suspend fun onLoad() {
        appScope.launch {
            bridgeLocalRepository.observeClaimBundles()
                .combine(bridgeLocalRepository.observeSendDetails()) { claimBundles, sendDetails ->
                    async { observeClaimBundles(claimBundles) }
                    async { observeSendDetails(sendDetails) }
                }
        }
    }

    override suspend fun onRefresh(): Unit = Unit

    private suspend fun observeClaimBundles(claimBundles: List<BridgeBundle>) {
        claimBundles.filter(BridgeBundle::isFinalized).forEach { claimBundle ->
            val bundleId = claimBundle.bundleId
            transactionManager.getTransactionStateFlow(bundleId)
                .firstOrNull()
                ?.let { state ->
                    if (state !is TransactionState.ClaimProgress) return@let
                    val newState = TransactionState.ClaimSuccess(
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
            val newState = TransactionState.BridgeSendSuccess(
                transactionId = bundleId,
                sendDetails = sendDetail
            )
            transactionManager.emitTransactionState(
                transactionId = bundleId,
                state = newState
            )
        }
    }
}
