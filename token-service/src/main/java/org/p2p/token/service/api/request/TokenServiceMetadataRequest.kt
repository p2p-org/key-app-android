package org.p2p.token.service.api.request

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc
import org.p2p.token.service.api.response.TokenItemMetadataResponse
import org.p2p.token.service.api.response.TokenServiceQueryResponse

internal data class TokenServiceMetadataRequest(
    @Transient val request: TokenServiceQueryRequest
) : JsonRpc<TokenServiceQueryRequest, List<TokenServiceQueryResponse<TokenItemMetadataResponse>>>(
    method = "get_tokens_info",
    params = request
) {
    @Transient
    override val typeOfResult: Type =
        object : TypeToken<List<TokenServiceQueryResponse<TokenItemMetadataResponse>>>() {}.type
}
