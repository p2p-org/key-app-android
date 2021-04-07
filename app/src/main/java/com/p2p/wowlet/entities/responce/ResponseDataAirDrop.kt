package com.p2p.wowlet.entities.responce

import com.squareup.moshi.Json

data class ResponseDataAirDrop(
    @field:Json(name = "jsonrpc")
    val jsonrpc: String,
    @field:Json(name = "id")
    val id: Int,
    @field:Json(name = "result")
    val result: String?,
    @field:Json(name = "error")
    val error: ErrorResponse?
)