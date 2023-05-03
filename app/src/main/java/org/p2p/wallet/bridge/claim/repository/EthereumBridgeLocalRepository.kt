package org.p2p.wallet.bridge.claim.repository

import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails
import org.p2p.wallet.transaction.model.NewShowProgress

interface EthereumBridgeLocalRepository {
    fun saveProgressDetails(bundleId: String, progressDetails: NewShowProgress)
    fun getProgressDetails(bundleId: String): NewShowProgress?

    fun saveBundles(items: List<BridgeBundle>)
    fun getBundle(bundleId: String): BridgeBundle?
    fun getAllBundles(): List<BridgeBundle>
    fun getAllSendDetails(): List<BridgeSendTransactionDetails>

    fun saveSendDetails(items: List<BridgeSendTransactionDetails>)
    fun getSendDetails(id: String): BridgeSendTransactionDetails?
}
