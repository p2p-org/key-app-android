package com.p2p.wowlet.dashboard.model.orderbook

import com.google.gson.annotations.SerializedName

data class Bid(
    @SerializedName("price")
    val price: Double,
    @SerializedName("size")
    val size: Double
)