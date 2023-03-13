package org.p2p.ethereumkit.internal.api.jsonrpc

open class DataJsonRpc(
    method: String,
    params: List<Any>
) : JsonRpc<List<Any>, ByteArray>(method, params) {
    @Transient
    override val typeOfResult = ByteArray::class.java
}
