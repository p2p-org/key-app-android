package org.p2p.ethereumkit.internal.api.jsonrpc

import com.google.gson.reflect.TypeToken
import org.p2p.ethereumkit.internal.api.jsonrpc.models.RpcBlock
import java.lang.reflect.Type

class GetBlockByNumberJsonRpc(
        @Transient val blockNumber: Long
) : JsonRpc<List<Any>,RpcBlock>(
        method = "eth_getBlockByNumber",
        params = listOf(blockNumber, false)
) {
    @Transient
    override val typeOfResult: Type = object : TypeToken<RpcBlock>() {}.type
}
