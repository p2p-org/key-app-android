package com.p2p.wowlet.dashboard.model.orderbook

import com.google.gson.annotations.SerializedName

data class OrderBooks(
    @SerializedName("market")
    val market: String,
    @SerializedName("bids")
    val bids: List<Bid>,
    @SerializedName("asks")
    val asks: List<Ask>,
    @SerializedName("marketAddress")
    val marketAddress: String
)