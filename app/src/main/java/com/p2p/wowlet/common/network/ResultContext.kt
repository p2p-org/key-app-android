package com.p2p.wowlet.common.network

import com.squareup.moshi.Json

data class ResultContext(
    @field:Json(name = "slot")
    val slot: Int
)