package com.p2p.wallet.infrastructure.network

import com.google.gson.annotations.SerializedName

data class ServerError(
    @SerializedName("error")
    val error: ErrorData
)

data class ErrorData(
    @SerializedName("code")
    val code: ErrorCode,

    @SerializedName("message")
    val message: String
)