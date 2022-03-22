package org.p2p.wallet.auth.api

import com.google.gson.annotations.SerializedName

data class LookupUsernameResponse(
    @SerializedName("address") val address: String,
    @SerializedName("name") val name: String,
    @SerializedName("parent") val parent: String,
)
