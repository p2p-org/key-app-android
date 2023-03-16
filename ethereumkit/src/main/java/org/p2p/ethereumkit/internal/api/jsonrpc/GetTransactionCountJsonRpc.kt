package org.p2p.ethereumkit.internal.api.jsonrpc

import org.p2p.core.wrapper.eth.EthAddress
import org.p2p.ethereumkit.internal.models.DefaultBlockParameter

class GetTransactionCountJsonRpc(
    @Transient val address: EthAddress,
    @Transient val defaultBlockParameter: DefaultBlockParameter
) : LongJsonRpc(
        method = "eth_getTransactionCount",
        params = listOf(address, defaultBlockParameter)
)
