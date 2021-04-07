package com.p2p.wowlet.entities.responce.orderbook


import com.squareup.moshi.Json

data class Bid(
    @Json(name = "price")
    val price: Double,
    @Json(name = "size")
    val size: Double
)