package com.p2p.wallet.common.network

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
)