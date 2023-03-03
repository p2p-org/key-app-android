package org.p2p.ethereumkit.internal.api.jsonrpc

import org.p2p.ethereumkit.internal.models.EthAddress
import org.p2p.ethereumkit.internal.models.DefaultBlockParameter

class GetStorageAtJsonRpc(
    @Transient val contractAddress: EthAddress,
    @Transient val position: ByteArray,
    @Transient val defaultBlockParameter: DefaultBlockParameter
) : DataJsonRpc(
        method = "eth_getStorageAt",
        params = listOf(contractAddress, position, defaultBlockParameter)
)
