package com.wowlet.entities.local


import com.squareup.moshi.Json

data class Orderbooks(
    @Json(name = "success")
    val success: Boolean,
    @Json(name = "data")
    val `data`: Data
)