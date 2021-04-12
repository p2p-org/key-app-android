package com.p2p.wallet.dashboard.model.orderbook

import com.google.gson.annotations.SerializedName

data class Ask(
    @SerializedName("price")
    val price: Double,
    @SerializedName("size")
    val size: Double
)