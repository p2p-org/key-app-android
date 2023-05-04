package org.p2p.wallet.bridge.claim.repository

import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails
import org.p2p.wallet.transaction.model.NewShowProgress

class EthereumBridgeInMemoryRepository : EthereumBridgeLocalRepository {

    private val bridgeBundlesMap: MutableMap<String, BridgeBundle> = mutableMapOf()
    private val bundleProgressDetailsMap: MutableMap<String, NewShowProgress> = mutableMapOf()
    private val bridgeSendDetailsMap: MutableMap<String, BridgeSendTransactionDetails> = mutableMapOf()

    override fun saveProgressDetails(bundleId: String, progressDetails: NewShowProgress) {
        bundleProgressDetailsMap[bundleId] = progressDetails
    }

    override fun getProgressDetails(bundleId: String): NewShowProgress? {
        return bundleProgressDetailsMap[bundleId]
    }

    override fun saveBundles(items: List<BridgeBundle>) {
        items.forEach { bridgeBundle ->
            bridgeBundlesMap[bridgeBundle.bundleId] = bridgeBundle
        }
    }

    override fun getBundle(bundleId: String): BridgeBundle? {
        return bridgeBundlesMap[bundleId]
    }

    override fun getAllBundles(): List<BridgeBundle> {
        return bridgeBundlesMap.values.toList()
    }

    override fun getAllSendDetails(): List<BridgeSendTransactionDetails> {
        return bridgeSendDetailsMap.values.toList()
    }

    override fun saveSendDetails(items: List<BridgeSendTransactionDetails>) {
        items.forEach { sendDetails ->
            bridgeSendDetailsMap[sendDetails.id] = sendDetails
        }
    }

    override fun getSendDetails(id: String): BridgeSendTransactionDetails? {
        return bridgeSendDetailsMap[id]
    }
}
