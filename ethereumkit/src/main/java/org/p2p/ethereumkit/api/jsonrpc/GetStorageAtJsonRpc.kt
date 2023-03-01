package org.p2p.ethereumkit.api.jsonrpc

import org.p2p.ethereumkit.models.EthAddress
import org.p2p.ethereumkit.models.DefaultBlockParameter

class GetStorageAtJsonRpc(
    @Transient val contractAddress: EthAddress,
    @Transient val position: ByteArray,
    @Transient val defaultBlockParameter: DefaultBlockParameter
) : DataJsonRpc(
        method = "eth_getStorageAt",
        params = listOf(contractAddress, position, defaultBlockParameter)
)
