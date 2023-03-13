package org.p2p.wallet.claim.api.request

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.lang.reflect.Type
import org.p2p.ethereumkit.internal.api.jsonrpc.JsonRpc
import org.p2p.wallet.claim.model.BridgeBundle

class SendBundleRpcRequest(
    @Transient val bundleRequest: BridgeBundle
) : JsonRpc<Map<String, Any>, JsonObject>(
    method = "send_bundle",
    params = buildMap { put("bundle", bundleRequest) }
) {
    override val typeOfResult: Type = Gson::class.java
}
