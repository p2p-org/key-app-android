package org.p2p.ethereumkit.internal.api.jsonrpc

import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.ethereumkit.internal.models.DefaultBlockParameter

class CallJsonRpc(
    @Transient val contractAddress: EthAddress,
    @Transient val data: ByteArray,
    @Transient val defaultBlockParameter: DefaultBlockParameter
) : DataJsonRpc(
        method = "eth_call",
        params = listOf(mapOf("to" to contractAddress, "data" to data), defaultBlockParameter)
)
