package org.p2p.wallet.bridge.claim.repository

import kotlinx.coroutines.flow.SharedFlow
import org.p2p.wallet.bridge.model.BridgeBundle
import org.p2p.wallet.bridge.send.model.BridgeSendTransactionDetails

interface EthereumBridgeLocalRepository {
    suspend fun saveClaimBundles(items: List<BridgeBundle>)
    fun getClaimBundleById(bundleId: String): BridgeBundle?
    fun getClaimBundleByKey(claimKey: String): BridgeBundle?
    fun getAllClaimBundles(): List<BridgeBundle>
    fun getAllSendDetails(): List<BridgeSendTransactionDetails>

    suspend fun saveSendDetails(items: List<BridgeSendTransactionDetails>)
    fun getSendDetails(id: String): BridgeSendTransactionDetails?

    fun observeClaimBundles(): SharedFlow<List<BridgeBundle>>
    fun observeSendDetails(): SharedFlow<List<BridgeSendTransactionDetails>>
}
