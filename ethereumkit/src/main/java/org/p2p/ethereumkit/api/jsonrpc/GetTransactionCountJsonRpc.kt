package org.p2p.ethereumkit.api.jsonrpc

import org.p2p.ethereumkit.models.EthAddress
import org.p2p.ethereumkit.models.DefaultBlockParameter

class GetTransactionCountJsonRpc(
    @Transient val address: EthAddress,
    @Transient val defaultBlockParameter: DefaultBlockParameter
) : LongJsonRpc(
        method = "eth_getTransactionCount",
        params = listOf(address, defaultBlockParameter)
)
