package com.p2p.wowlet.common.network

import com.squareup.moshi.Json

data class ResponseData(
    @Json(name = "jsonrpc")
    val jsonrpc: String,
    @Json(name = "id")
    val id: Int,
    @Json(name = "result")
    val result: ResultSuccess?,
    @Json(name = "error")
    val error: ErrorResponse?
)