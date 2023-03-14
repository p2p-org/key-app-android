package org.p2p.wallet.bridge.api.request

import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc
import org.p2p.wallet.bridge.model.BridgeBundle

data class SendEthereumBundleRpcRequest(
    @Transient val bundleRequest: BridgeBundle,
) : JsonRpc<Map<String, Any>, Unit>(
    method = "send_ethereum_bundle",
    params = buildMap { put("bundle", bundleRequest) }
) {
    override val typeOfResult: Type = Unit::class.java
}
