package com.p2p.wowlet.dashboard.model.orderbook

import com.squareup.moshi.Json

data class Ask(
    @Json(name = "price")
    val price: Double,
    @Json(name = "size")
    val size: Double
)