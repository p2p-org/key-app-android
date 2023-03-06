package org.p2p.ethereumkit.external.api.request

import com.google.gson.JsonElement
import org.p2p.ethereumkit.external.api.response.TokenBalancesResponse
import org.p2p.ethereumkit.internal.api.jsonrpc.JsonRpc
import org.p2p.ethereumkit.internal.models.EthAddress
import java.lang.reflect.Type
import java.math.BigInteger

internal data class GetTokenBalancesJsonRpc(
    @Transient val address: EthAddress
) : JsonRpc<TokenBalancesResponse>(
    method = "alchemy_getTokenBalances",
    params = listOf(address)
) {
    @Transient
    override val typeOfResult: Type = TokenBalancesResponse::class.java
}
