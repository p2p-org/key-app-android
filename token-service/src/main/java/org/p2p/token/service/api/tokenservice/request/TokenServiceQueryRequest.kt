package org.p2p.token.service.api.tokenservice.request

import com.google.gson.annotations.SerializedName

internal class TokenServiceQueryRequest(
    @SerializedName("query")
    val query: List<TokenServiceItemRequest>
)
