package org.p2p.wallet.home.api

import com.google.gson.annotations.SerializedName

data class OrderBooksResponse(
    @SerializedName("data")
    val data: OrderBooksDataResponse
)

data class OrderBooksDataResponse(
    @SerializedName("bids")
    val bids: List<BidResponse>
)

data class BidResponse(
    @SerializedName("price")
    val price: Double,
    @SerializedName("size")
    val size: Double
)
