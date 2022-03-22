package org.p2p.wallet.auth.model

import com.google.gson.annotations.SerializedName

data class RegisterNameRequest(
    @SerializedName("owner")
    val owner: String,
    @SerializedName("credentials")
    val credentials: Credentials
)
