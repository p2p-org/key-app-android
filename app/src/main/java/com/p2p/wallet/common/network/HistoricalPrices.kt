package com.p2p.wallet.common.network

import com.google.gson.annotations.SerializedName

data class HistoricalPrices(
    @SerializedName("close")
    val close: Double,
    @SerializedName("open")
    val open: Double,
    @SerializedName("low")
    val low: Double,
    @SerializedName("high")
    val high: Double,
    @SerializedName("startTime")
    val startTime: Long,
    @SerializedName("market")
    val market: String,
    @SerializedName("volumeBase")
    val volumeBase: Double,
    @SerializedName("volumeQuote")
    val volumeQuote: Double
)