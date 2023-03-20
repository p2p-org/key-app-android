package org.p2p.wallet.bridge.api.request

import java.lang.reflect.Type
import java.util.Optional
import org.p2p.core.rpc.JsonRpc
import org.p2p.core.token.SolAddress
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.wallet.bridge.api.response.BridgeBundleFeesResponse

data class GetEthereumFeesRpcRequest(
    @Transient val ethAddress: EthAddress,
    @Transient val recipientAddress: SolAddress,
    @Transient val erc20Token: Optional<EthAddress>,
    @Transient val amount: String,
) : JsonRpc<Map<String, Any>, BridgeBundleFeesResponse>(
    method = "get_ethereum_fees",
    params = buildMap {
        put("user_wallet", ethAddress)
        put("recipient", recipientAddress)
        put("token", erc20Token)
        put("amount", amount)
    }
) {

    @Transient
    override val typeOfResult: Type = BridgeBundleFeesResponse::class.java
}
