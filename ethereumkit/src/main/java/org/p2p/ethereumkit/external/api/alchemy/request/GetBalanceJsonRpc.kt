package org.p2p.ethereumkit.external.api.alchemy.request

import org.p2p.core.rpc.JsonRpc
import org.p2p.core.wrapper.eth.EthAddress
import java.lang.reflect.Type
import java.math.BigInteger
import org.p2p.core.model.DefaultBlockParameter

internal class GetBalanceJsonRpc(
    @Transient val address: EthAddress,
    @Transient val defaultBlockParameter: DefaultBlockParameter
) : JsonRpc<List<Any>, BigInteger>(
    method = "eth_getBalance",
    params = listOf(address, defaultBlockParameter)
) {
    @Transient
    override val typeOfResult: Type = BigInteger::class.java
}
