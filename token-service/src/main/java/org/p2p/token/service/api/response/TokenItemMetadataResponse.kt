package org.p2p.token.service.api.response

import com.google.gson.annotations.SerializedName

internal data class TokenItemMetadataResponse(
    @SerializedName("address")
    val address: String,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("logo_url")
    val logoUrl: String,
    @SerializedName("decimals")
    val decimals: Int,
    @SerializedName("price")
    val price: TokenItemPriceResponse
)
