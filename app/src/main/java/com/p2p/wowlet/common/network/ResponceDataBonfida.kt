package com.p2p.wowlet.common.network

import com.squareup.moshi.Json

data class ResponceDataBonfida<T>(
    @Json(name = "success")
    val success: Boolean,
    @Json(name = "data")
    val data: T
)