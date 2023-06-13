package org.p2p.wallet.striga.user.api

import com.google.gson.annotations.SerializedName

data class StrigaStartKycResponse(
    @SerializedName("provider")
    val provider: String,
    @SerializedName("token")
    val accessToken: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("verificationLink")
    val verificationLink: String
)
