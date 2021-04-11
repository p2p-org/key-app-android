package com.p2p.wowlet.entities.responce.orderbook

import com.squareup.moshi.Json

data class OrderBooks(
    @Json(name = "market")
    val market: String,
    @Json(name = "bids")
    val bids: List<Bid>,
    @Json(name = "asks")
    val asks: List<Ask>,
    @Json(name = "marketAddress")
    val marketAddress: String
)