package com.p2p.wallet.infrastructure.network

import com.google.gson.annotations.SerializedName

data class CommonResponse<Result>(
    @SerializedName("result")
    val result: Result,

    @SerializedName("id")
    val id: String
)