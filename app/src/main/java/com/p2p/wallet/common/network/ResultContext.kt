package com.p2p.wallet.common.network

import com.google.gson.annotations.SerializedName

data class ResultContext(
    @SerializedName("slot")
    val slot: Int
)