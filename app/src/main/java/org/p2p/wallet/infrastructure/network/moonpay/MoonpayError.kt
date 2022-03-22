package org.p2p.wallet.infrastructure.network.moonpay

import com.google.gson.annotations.SerializedName

data class MoonpayError(
    @SerializedName("message")
    val message: String,
    @SerializedName("type")
    val type: String
)
