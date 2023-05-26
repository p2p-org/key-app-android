package org.p2p.wallet.bridge.api.request

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc
import org.p2p.core.token.SolAddress
import org.p2p.wallet.bridge.api.response.BridgeTransactionStatusResponse

data class SolanaTransferStatusesRpcRequest(
    @Transient val userWallet: SolAddress,
) : JsonRpc<Map<String, Any>, List<BridgeTransactionStatusResponse>>(
    method = "list_solana_statuses",
    params = buildMap {
        put("user_wallet", userWallet)
    }
) {
    @Transient
    override val typeOfResult: Type = object : TypeToken<List<BridgeTransactionStatusResponse>>() {}.type
}
