package org.p2p.ethereumkit.external.api.alchemy.request

import org.p2p.ethereumkit.external.api.alchemy.response.TokenBalancesResponse
import org.p2p.core.rpc.JsonRpc
import org.p2p.core.wrapper.eth.EthAddress
import java.lang.reflect.Type

internal data class GetTokenBalancesJsonRpc(
    @Transient val address: EthAddress,
    @Transient val tokenAddresses: List<EthAddress>
) : JsonRpc<List<Any>, TokenBalancesResponse>(
    method = "alchemy_getTokenBalances",
    params = listOf(address, tokenAddresses)
) {
    @Transient
    override val typeOfResult: Type = TokenBalancesResponse::class.java
}
