package org.p2p.ethereumkit.internal.api.jsonrpc

import com.google.gson.reflect.TypeToken
import org.p2p.ethereumkit.internal.api.jsonrpc.models.RpcTransaction
import java.lang.reflect.Type

class GetTransactionByHashJsonRpc(
        @Transient val transactionHash: ByteArray
) : JsonRpc<List<Any>,RpcTransaction>(
        method = "eth_getTransactionByHash",
        params = listOf(transactionHash)
) {
    @Transient
    override val typeOfResult: Type = object : TypeToken<RpcTransaction>() {}.type
}
