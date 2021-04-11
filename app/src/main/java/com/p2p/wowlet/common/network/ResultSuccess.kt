package com.p2p.wowlet.common.network

import com.squareup.moshi.Json

data class ResultSuccess(
    @field:Json(name = "context")
    val context: ResultContext,
    @field:Json(name = "value")
    val value: Int,
)