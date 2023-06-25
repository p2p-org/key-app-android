package org.p2p.market.price.api.response

import com.google.gson.annotations.SerializedName

internal data class MarketPriceQueryResponse(
    @SerializedName("chain_id")
    val marketPriceChain: NetworkChain,
    @SerializedName("data")
    val marketPriceItemResponse: List<MarketPriceItemResponse>
)




