package org.p2p.wallet.bridge.api.request

import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc
import org.p2p.core.token.SolAddress
import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.wallet.bridge.api.response.BridgeBundleResponse

data class GetEthereumBundleRpcRequest(
    @Transient val ethAddress: EthAddress,
    @Transient val recipientAddress: SolAddress,
    @Transient val erc20Token: EthAddress?,
    @Transient val amount: String,
    @Transient val slippage: Int?,
) : JsonRpc<Map<String, Any>, BridgeBundleResponse>(
    method = "get_ethereum_bundle",
    params = buildMap {
        put("user_wallet", ethAddress)
        put("recipient", recipientAddress)
        erc20Token?.let { put("token", it) }
        put("amount", amount)
        slippage?.let { put("slippage", it) }
    }
) {
    @Transient
    override val typeOfResult: Type = BridgeBundleResponse::class.java
}
