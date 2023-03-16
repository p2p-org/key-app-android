package org.p2p.ethereumkit.internal.api.jsonrpc

import org.p2p.core.rpc.JsonRpc

open class DataJsonRpc(
    method: String,
    params: List<Any>
) : JsonRpc<List<Any>, ByteArray>(method, params) {
    @Transient
    override val typeOfResult = ByteArray::class.java
}
