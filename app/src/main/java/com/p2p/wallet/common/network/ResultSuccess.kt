package com.p2p.wallet.common.network

import com.google.gson.annotations.SerializedName

data class ResultSuccess(
    @SerializedName("context")
    val context: ResultContext,
    @SerializedName("value")
    val value: Int,
)