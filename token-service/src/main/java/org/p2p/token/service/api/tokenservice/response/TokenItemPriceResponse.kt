package org.p2p.token.service.api.tokenservice.response

import com.google.gson.annotations.SerializedName

internal class TokenItemPriceResponse(
    @SerializedName("address")
    val tokenAddress: String,
    @SerializedName("price")
    val price: TokenRateResponse?
)
