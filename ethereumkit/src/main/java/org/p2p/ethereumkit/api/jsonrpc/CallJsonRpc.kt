package org.p2p.ethereumkit.api.jsonrpc

import org.p2p.ethereumkit.models.EthAddress
import org.p2p.ethereumkit.models.DefaultBlockParameter

class CallJsonRpc(
    @Transient val contractAddress: EthAddress,
    @Transient val data: ByteArray,
    @Transient val defaultBlockParameter: DefaultBlockParameter
) : DataJsonRpc(
        method = "eth_call",
        params = listOf(mapOf("to" to contractAddress, "data" to data), defaultBlockParameter)
)
