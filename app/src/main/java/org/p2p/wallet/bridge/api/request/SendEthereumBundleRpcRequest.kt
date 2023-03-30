package org.p2p.wallet.bridge.api.request

import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc
import org.p2p.wallet.bridge.api.response.BridgeBundleResponse

data class SendEthereumBundleRpcRequest(
    @Transient val bundleRequest: BridgeBundleResponse,
) : JsonRpc<Map<String, Any>, Unit>(
    method = "send_ethereum_bundle", // "simulate_ethereum_bundle",
    params = buildMap { put("bundle", bundleRequest) }
) {

    @Transient
    override val typeOfResult: Type = Unit::class.java
}
