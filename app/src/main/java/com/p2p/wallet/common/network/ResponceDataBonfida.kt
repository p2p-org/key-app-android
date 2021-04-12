package com.p2p.wallet.common.network

import com.google.gson.annotations.SerializedName

data class ResponceDataBonfida<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val data: T
)