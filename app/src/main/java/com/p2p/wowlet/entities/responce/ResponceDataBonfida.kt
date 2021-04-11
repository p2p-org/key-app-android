package com.p2p.wowlet.entities.responce

import com.squareup.moshi.Json

data class ResponceDataBonfida<T>(
    @Json(name = "success")
    val success: Boolean,
    @Json(name = "data")
    val data: T
)