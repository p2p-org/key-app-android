package com.p2p.wowlet.common.network

import com.google.gson.annotations.SerializedName

data class ResultContext(
    @SerializedName("slot")
    val slot: Int
)