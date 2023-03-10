package org.p2p.ethereumkit.external.api.alchemy.request

import org.p2p.ethereumkit.internal.api.jsonrpc.JsonRpc
import org.p2p.ethereumkit.internal.models.DefaultBlockParameter
import org.p2p.ethereumkit.internal.models.EthAddress
import java.lang.reflect.Type
import java.math.BigInteger

internal class GetBalanceJsonRpc(
    @Transient val address: EthAddress,
    @Transient val defaultBlockParameter: DefaultBlockParameter
) : JsonRpc<BigInteger>(
    method = "eth_getBalance",
    params = listOf(address, defaultBlockParameter)
) {
    @Transient
    override val typeOfResult: Type = BigInteger::class.java
}
