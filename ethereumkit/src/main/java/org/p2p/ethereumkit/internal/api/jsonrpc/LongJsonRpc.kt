package org.p2p.ethereumkit.internal.api.jsonrpc

import org.p2p.core.rpc.JsonRpc

open class LongJsonRpc(
        method: String, params: List<Any>
) : JsonRpc<List<Any>, Long>(method, params) {
    @Transient
    override val typeOfResult = Long::class.java
}
