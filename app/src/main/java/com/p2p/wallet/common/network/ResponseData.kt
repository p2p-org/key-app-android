package com.p2p.wallet.common.network

import com.google.gson.annotations.SerializedName

data class ResponseData(
    @SerializedName("jsonrpc")
    val jsonrpc: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("result")
    val result: ResultSuccess?,
    @SerializedName("error")
    val error: ErrorResponse?
)