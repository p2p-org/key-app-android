package com.p2p.wallet.auth.api

import com.google.gson.annotations.SerializedName

data class CheckCaptchaResponse(
    @SerializedName("success") val success: Int,
    @SerializedName("challenge") val challenge: String,
    @SerializedName("gt") val gt: String,
    @SerializedName("new_captcha") val newCaptcha: Boolean,
)