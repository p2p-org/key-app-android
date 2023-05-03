package org.p2p.wallet.bridge.claim.repository

import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.transaction.model.NewShowProgress

interface EthereumClaimLocalRepository {
    fun saveProgressDetails(bundleId: String, progressDetails: NewShowProgress)
    fun getProgressDetails(bundleId: String): NewShowProgress?

    fun saveBundles(items: List<BridgeBundle>)
    fun getBundle(bundleId: String): BridgeBundle?
}
