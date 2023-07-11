package org.p2p.wallet.bridge.claim.repository

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails

class EthereumBridgeInMemoryRepository : EthereumBridgeLocalRepository {

    private val bridgeBundlesMap: MutableMap<String, BridgeBundle> = mutableMapOf()
    private val bridgeSendDetailsMap: MutableMap<String, BridgeSendTransactionDetails> = mutableMapOf()

    private val bridgeBundlesFlow = MutableSharedFlow<List<BridgeBundle>>()
    private val sendDetailsFlow = MutableSharedFlow<List<BridgeSendTransactionDetails>>()

    override suspend fun saveClaimBundles(items: List<BridgeBundle>) {
        items.forEach { bridgeBundle ->
            bridgeBundlesMap[bridgeBundle.bundleId] = bridgeBundle
        }
        bridgeBundlesFlow.emit(items)
    }

    override fun getClaimBundleByKey(claimKey: String): BridgeBundle? {
        return bridgeBundlesMap.values.find { it.claimKey == claimKey }
    }

    override fun getClaimBundleById(bundleId: String): BridgeBundle? {
        return bridgeBundlesMap[bundleId]
    }

    override fun getAllClaimBundles(): List<BridgeBundle> {
        return bridgeBundlesMap.values.toList()
    }

    override fun getAllSendDetails(): List<BridgeSendTransactionDetails> {
        return bridgeSendDetailsMap.values.toList()
    }

    override suspend fun saveSendDetails(items: List<BridgeSendTransactionDetails>) {
        items.forEach { sendDetails ->
            bridgeSendDetailsMap[sendDetails.id] = sendDetails
        }
        sendDetailsFlow.emit(items)
    }

    override fun getSendDetails(id: String): BridgeSendTransactionDetails? {
        return bridgeSendDetailsMap[id]
    }

    override fun observeClaimBundles(): SharedFlow<List<BridgeBundle>> {
        return bridgeBundlesFlow
    }

    override fun observeSendDetails(): SharedFlow<List<BridgeSendTransactionDetails>> {
        return sendDetailsFlow
    }
}
