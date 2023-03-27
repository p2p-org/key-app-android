package org.p2p.wallet.bridge.api.request

import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc
import org.p2p.wallet.bridge.api.response.BridgeTransactionStatusResponse

data class SolanaTransferStatusRpcRequest(
    @Transient val message: String,
) : JsonRpc<Map<String, Any>, BridgeTransactionStatusResponse>(
    method = "get_solana_transfer_status",
    params = buildMap {
        put("message", message)
    }
) {
    override val typeOfResult: Type = BridgeTransactionStatusResponse::class.java
}
