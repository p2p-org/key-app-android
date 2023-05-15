package org.p2p.wallet.bridge.claim.repository

import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails

interface EthereumBridgeLocalRepository {
    fun saveClaimBundles(items: List<BridgeBundle>)
    fun getClaimBundleById(bundleId: String): BridgeBundle?
    fun getClaimBundleByKey(claimKey: String): BridgeBundle?
    fun getAllClaimBundles(): List<BridgeBundle>
    fun getAllSendDetails(): List<BridgeSendTransactionDetails>

    fun saveSendDetails(items: List<BridgeSendTransactionDetails>)
    fun getSendDetails(id: String): BridgeSendTransactionDetails?
}
