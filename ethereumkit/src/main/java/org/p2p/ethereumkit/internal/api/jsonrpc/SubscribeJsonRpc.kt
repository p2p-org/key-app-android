package org.p2p.ethereumkit.internal.api.jsonrpc

import org.p2p.core.rpc.JsonRpc

class SubscribeJsonRpc(
        params: List<Any>
) : JsonRpc<List<Any>, String>("eth_subscribe", params) {
    @Transient
    override val typeOfResult = String::class.java
}
