package org.p2p.wallet.bridge.claim.repository

import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.transaction.model.NewShowProgress

class EthereumClaimInMemoryRepository : EthereumClaimLocalRepository {

    private val bridgeBundlesMap: MutableMap<String, BridgeBundle> = mutableMapOf()
    private val bundleProgressDetailsMap: MutableMap<String, NewShowProgress> = mutableMapOf()

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
}
