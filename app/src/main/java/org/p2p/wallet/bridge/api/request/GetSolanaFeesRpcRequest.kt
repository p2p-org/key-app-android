package org.p2p.wallet.bridge.api.request

import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc
import org.p2p.core.token.SolAddress
import org.p2p.wallet.bridge.api.response.BridgeSendFeesResponse

data class GetSolanaFeesRpcRequest(
    @Transient val userWallet: SolAddress,
    @Transient val recipient: SolAddress,
    @Transient val mint: SolAddress?,
    @Transient val amount: String,
) : JsonRpc<Map<String, Any>, BridgeSendFeesResponse>(
    method = "get_send_fees",
    params = buildMap {
        put("user_wallet", userWallet)
        put("recipient", recipient)
        mint?.let { put("mint", mint) }
        put("amount", amount)
    }
) {

    @Transient
    override val typeOfResult: Type = BridgeSendFeesResponse::class.java
}
