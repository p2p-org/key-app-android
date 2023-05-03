package org.p2p.wallet.bridge.claim.repository

import org.p2p.wallet.transaction.model.NewShowProgress

class EthereumClaimLocalRepository {

    private val bundleProgressDetailsMap: MutableMap<String, NewShowProgress> = mutableMapOf()

    fun saveProgressDetails(bundleId: String, progressDetails: NewShowProgress) {
        bundleProgressDetailsMap[bundleId] = progressDetails
    }

    fun getProgressDetails(bundleId: String): NewShowProgress? {
        return bundleProgressDetailsMap[bundleId]
    }
}
