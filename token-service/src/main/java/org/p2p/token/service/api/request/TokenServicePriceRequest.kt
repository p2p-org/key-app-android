package org.p2p.token.service.api.request

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc
import org.p2p.token.service.api.response.TokenItemPriceResponse
import org.p2p.token.service.api.response.TokenServiceQueryResponse

internal data class TokenServicePriceRequest(
    @Transient val request: TokenServiceQueryRequest
) : JsonRpc<TokenServiceQueryRequest, List<TokenServiceQueryResponse<TokenItemPriceResponse>>>(
    method = "get_tokens_price",
    params = request
) {
    @Transient
    override val typeOfResult: Type =
        object : TypeToken<List<TokenServiceQueryResponse<TokenItemPriceResponse>>>() {}.type
}
