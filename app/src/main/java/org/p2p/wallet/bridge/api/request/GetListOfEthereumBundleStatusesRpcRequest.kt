package org.p2p.wallet.bridge.api.request

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc
import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.wallet.bridge.api.response.BridgeBundleResponse

data class GetListOfEthereumBundleStatusesRpcRequest(
    @Transient val ethAddress: EthAddress
) : JsonRpc<Map<String, Any>, List<BridgeBundleResponse>>(
    method = "list_ethereum_bundles",
    params = buildMap {
        put("user_wallet", ethAddress)
    }
) {
    @Transient
    override val typeOfResult: Type = object : TypeToken<List<BridgeBundleResponse>>() {}.type
}
