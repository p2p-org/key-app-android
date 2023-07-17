package org.p2p.token.service.api.request

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc
import org.p2p.token.service.api.response.TokenServiceMetadataResponse

private typealias MetadataResponse = List<TokenServiceMetadataResponse>

internal data class TokenServiceMetadataRequest(
    @Transient val request: TokenServiceQueryRequest
) : JsonRpc<TokenServiceQueryRequest, MetadataResponse>(
    method = "get_tokens_info",
    params = request
) {
    @Transient
    override val typeOfResult: Type =
        object : TypeToken<MetadataResponse>() {}.type
}
