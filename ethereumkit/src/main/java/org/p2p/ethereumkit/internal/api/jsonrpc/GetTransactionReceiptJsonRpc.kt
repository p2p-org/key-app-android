package org.p2p.ethereumkit.internal.api.jsonrpc

import com.google.gson.reflect.TypeToken
import org.p2p.ethereumkit.internal.api.jsonrpc.models.RpcTransactionReceipt
import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc

class GetTransactionReceiptJsonRpc(
        @Transient val transactionHash: ByteArray
) : JsonRpc<List<Any>, RpcTransactionReceipt>(
        method = "eth_getTransactionReceipt",
        params = listOf(transactionHash)
) {
    @Transient
    override val typeOfResult: Type = object : TypeToken<RpcTransactionReceipt>() {}.type
}
