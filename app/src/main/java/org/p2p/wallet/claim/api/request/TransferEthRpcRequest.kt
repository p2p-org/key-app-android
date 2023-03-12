package org.p2p.wallet.claim.api.request

import java.lang.reflect.Type
import org.p2p.core.token.SolAddress
import org.p2p.ethereumkit.internal.api.jsonrpc.JsonRpc
import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.wallet.claim.api.response.BridgeBundleResponse

data class TransferEthRpcRequest(
    @Transient val userEthWallet: EthAddress,
    @Transient val solRecipient: SolAddress,
    @Transient val amount: String
) : JsonRpc<Map<String, Any>, BridgeBundleResponse>(
    method = "transfer_eth",
    params = buildMap {
        put("user_wallet", userEthWallet)
        put("recipient", solRecipient)
        put("amount", amount)
    }
) {
    override val typeOfResult: Type = BridgeBundleResponse::class.java
}
