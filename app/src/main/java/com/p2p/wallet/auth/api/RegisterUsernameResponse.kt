package com.p2p.wallet.auth.api

import com.google.gson.annotations.SerializedName

data class RegisterUsernameResponse(
    @SerializedName("signature") val signature: String,
)