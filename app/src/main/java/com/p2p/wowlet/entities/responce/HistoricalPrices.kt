package com.p2p.wowlet.entities.responce

import com.squareup.moshi.Json


data class HistoricalPrices(
    @Json(name = "close")
    val close: Double,
    @Json(name = "open")
    val open: Double,
    @Json(name = "low")
    val low: Double,
    @Json(name = "high")
    val high: Double,
    @Json(name = "startTime")
    val startTime: Long,
    @Json(name = "market")
    val market: String,
    @Json(name = "volumeBase")
    val volumeBase: Double,
    @Json(name = "volumeQuote")
    val volumeQuote: Double
)