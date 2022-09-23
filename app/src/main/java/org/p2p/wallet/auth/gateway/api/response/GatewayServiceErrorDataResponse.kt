package org.p2p.wallet.auth.gateway.api.response

import com.google.gson.annotations.SerializedName

data class GatewayServiceErrorDataResponse(
    @SerializedName("cooldown_ttl")
    val cooldownTtl: Long,
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String
)
