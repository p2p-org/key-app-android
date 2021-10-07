package com.p2p.wallet.auth.api

import com.google.gson.annotations.SerializedName

data class SendCaptchaResponse(
    @SerializedName("success") val success: Int,
)