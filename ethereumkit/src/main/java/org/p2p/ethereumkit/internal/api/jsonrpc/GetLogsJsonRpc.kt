package org.p2p.ethereumkit.internal.api.jsonrpc

import com.google.gson.reflect.TypeToken
import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.ethereumkit.internal.models.DefaultBlockParameter
import org.p2p.ethereumkit.internal.models.TransactionLog
import java.lang.reflect.Type

class GetLogsJsonRpc(
    @Transient val address: EthAddress?,
    @Transient val fromBlock: DefaultBlockParameter,
    @Transient val toBlock: DefaultBlockParameter,
    @Transient val topics: List<ByteArray?>
) : JsonRpc<ArrayList<TransactionLog>>(
        method = "eth_getLogs",
        params = listOf(GetLogsParams(address, fromBlock, toBlock, topics))
) {
    @Transient
    override val typeOfResult: Type = object : TypeToken<List<TransactionLog>>() {}.type

    data class GetLogsParams(
        val address: EthAddress?,
        val fromBlock: DefaultBlockParameter,
        val toBlock: DefaultBlockParameter,
        val topics: List<ByteArray?>
    )
}
