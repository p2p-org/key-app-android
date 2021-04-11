package com.p2p.wowlet.entities.responce

import com.squareup.moshi.Json

data class CallRequest(
    @field:Json(name = "method")
    val method: String,
    @field:Json(name = "params")
    val params: List<Any>,
    @field:Json(name = "jsonrpc")
    val jsonrpc: String = "2.0",
    @field:Json(name = "id")
    val id: Int = 1
)