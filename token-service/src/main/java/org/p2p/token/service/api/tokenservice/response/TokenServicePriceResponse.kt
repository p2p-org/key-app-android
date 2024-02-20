package org.p2p.token.service.api.tokenservice.response

import com.google.gson.annotations.SerializedName

internal data class TokenServicePriceResponse(
    @SerializedName("chain_id")
    val tokenServiceChainResponse: TokenServiceNetworkResponse,
    @SerializedName("data")
    val tokenServiceItemsResponse: List<TokenItemPriceResponse>
)
