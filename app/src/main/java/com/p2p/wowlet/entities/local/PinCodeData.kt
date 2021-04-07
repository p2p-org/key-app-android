package com.p2p.wowlet.entities.local

import com.squareup.moshi.Json

data class PinCodeData(
    @field:Json(name="pinCode")
    val pinCode: Int
)