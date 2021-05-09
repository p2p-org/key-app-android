package com.p2p.wallet.main.api

import com.google.gson.annotations.SerializedName

data class PriceHistoryResponse(
    @SerializedName("Data")
    val data: Data
)

data class Data(
    @SerializedName("Data")
    val list: List<History>
)

data class History(
    @SerializedName("close")
    val close: Double
)