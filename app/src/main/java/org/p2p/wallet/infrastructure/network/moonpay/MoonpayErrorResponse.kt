package org.p2p.wallet.infrastructure.network.moonpay

import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName

data class MoonpayErrorResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("type")
    val type: String?,
    @SerializedName("errors")
    val errors: JsonArray?
)
