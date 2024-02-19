package org.p2p.ethereumkit.external.api.alchemy.request

import java.lang.reflect.Type
import org.p2p.core.model.DefaultBlockParameter
import org.p2p.core.rpc.JsonRpc
import org.p2p.core.wrapper.eth.EthAddress

internal class GetBalanceJsonRpc(
    @Transient val address: EthAddress,
    @Transient val defaultBlockParameter: DefaultBlockParameter
) : JsonRpc<List<Any>, String>(
    method = "eth_getBalance",
    params = listOf(address, defaultBlockParameter)
) {
    @Transient
    override val typeOfResult: Type = String::class.java
}
