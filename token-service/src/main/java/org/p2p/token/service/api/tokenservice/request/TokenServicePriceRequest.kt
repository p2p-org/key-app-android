package org.p2p.token.service.api.tokenservice.request

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc
import org.p2p.token.service.api.tokenservice.response.TokenServicePriceResponse

private typealias PriceResponse = List<TokenServicePriceResponse>

internal data class TokenServicePriceRequest(
    @Transient val request: TokenServiceQueryRequest
) : JsonRpc<TokenServiceQueryRequest, PriceResponse>(
    method = "get_tokens_price",
    params = request
) {
    @Transient
    override val typeOfResult: Type =
        object : TypeToken<PriceResponse>() {}.type
}
