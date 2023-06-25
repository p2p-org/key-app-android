package org.p2p.market.price.api.response

import com.google.gson.annotations.SerializedName

internal class MarketPriceItemResponse(
    @SerializedName("address")
    val tokenAddress: String,
    @SerializedName("price")
    val price: MarketPriceResponse
)


