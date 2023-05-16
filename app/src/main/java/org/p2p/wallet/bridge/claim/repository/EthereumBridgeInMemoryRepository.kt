package org.p2p.wallet.bridge.claim.repository

import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails

class EthereumBridgeInMemoryRepository : EthereumBridgeLocalRepository {

    private val bridgeBundlesMap: MutableMap<String, BridgeBundle> = mutableMapOf()
    private val bridgeSendDetailsMap: MutableMap<String, BridgeSendTransactionDetails> = mutableMapOf()

    override fun saveClaimBundles(items: List<BridgeBundle>) {
        items.forEach { bridgeBundle ->
            bridgeBundlesMap[bridgeBundle.bundleId] = bridgeBundle
        }
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

    override fun saveSendDetails(items: List<BridgeSendTransactionDetails>) {
        items.forEach { sendDetails ->
            bridgeSendDetailsMap[sendDetails.id] = sendDetails
        }
    }

    override fun getSendDetails(id: String): BridgeSendTransactionDetails? {
        return bridgeSendDetailsMap[id]
    }
}
