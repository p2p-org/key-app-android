package org.p2p.token.service.api.response

import com.google.gson.annotations.SerializedName

internal data class TokenServiceQueryResponse<T>(
    @SerializedName("chain_id")
    val tokenServiceChainResponse: TokenServiceNetworkResponse,
    @SerializedName("data")
    val tokenServiceItemsResponse: List<T>
)
