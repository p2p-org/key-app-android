package com.p2p.wowlet.common.network

import com.google.gson.annotations.SerializedName

data class CallRequest(
    @SerializedName("method")
    val method: String,
    @SerializedName("params")
    val params: List<Any>,
    @SerializedName("jsonrpc")
    val jsonrpc: String = "2.0",
    @SerializedName("id")
    val id: Int = 1
)