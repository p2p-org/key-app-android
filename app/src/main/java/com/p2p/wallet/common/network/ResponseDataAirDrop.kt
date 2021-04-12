package com.p2p.wallet.common.network

import com.google.gson.annotations.SerializedName

data class ResponseDataAirDrop(
    @SerializedName("jsonrpc")
    val jsonrpc: String,
    @SerializedName("id")
    val id: Int,
    @SerializedName("result")
    val result: String?,
    @SerializedName("error")
    val error: ErrorResponse?
)