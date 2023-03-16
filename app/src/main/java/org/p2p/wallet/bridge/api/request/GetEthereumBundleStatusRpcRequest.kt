package org.p2p.wallet.bridge.api.request

import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc
import org.p2p.wallet.bridge.api.response.BridgeBundleResponse

data class GetEthereumBundleStatusRpcRequest(
    @Transient val bundleId: String,
) : JsonRpc<Map<String, Any>, BridgeBundleResponse>(
    method = "get_ethereum_bundle_status",
    params = buildMap {
        put("bundle_id", bundleId)
    }
) {
    @Transient
    override val typeOfResult: Type = BridgeBundleResponse::class.java
}
