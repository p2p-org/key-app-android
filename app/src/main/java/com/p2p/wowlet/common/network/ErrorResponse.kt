package com.p2p.wowlet.common.network

import com.squareup.moshi.Json

data class ErrorResponse(
    @field:Json(name = "code")
    val code: Int,
    @field:Json(name = "message")
    val message: String,
)