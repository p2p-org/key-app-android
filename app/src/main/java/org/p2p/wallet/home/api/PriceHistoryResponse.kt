package org.p2p.wallet.home.api

import com.google.gson.annotations.SerializedName

data class PriceHistoryResponse(
    @SerializedName("Data")
    val data: Data,

    @SerializedName("Message")
    val message: String?
)

data class Data(
    @SerializedName("Data")
    val list: List<History>
)

data class History(
    @SerializedName("close")
    val close: Double
)
