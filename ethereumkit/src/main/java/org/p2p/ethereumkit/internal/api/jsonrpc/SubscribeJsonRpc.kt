package org.p2p.ethereumkit.internal.api.jsonrpc

class SubscribeJsonRpc(
        params: List<Any>
) : JsonRpc<List<Any>,String>("eth_subscribe", params) {
    @Transient
    override val typeOfResult = String::class.java
}
