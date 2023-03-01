package org.p2p.ethereumkit.api.jsonrpc

import org.p2p.ethereumkit.models.EthAddress
import org.p2p.ethereumkit.models.DefaultBlockParameter
import java.math.BigInteger

class GetBalanceJsonRpc(
    @Transient val address: EthAddress,
    @Transient val defaultBlockParameter: DefaultBlockParameter
) : JsonRpc<BigInteger>(
        method = "eth_getBalance",
        params = listOf(address, defaultBlockParameter)
) {
    @Transient
    override val typeOfResult = BigInteger::class.java
}
