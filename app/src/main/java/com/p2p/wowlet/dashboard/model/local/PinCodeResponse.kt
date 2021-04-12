package com.p2p.wowlet.dashboard.model.local

import com.google.gson.annotations.SerializedName

data class PinCodeResponse(
    @SerializedName("pinCode")
    val pinCode: Int
)