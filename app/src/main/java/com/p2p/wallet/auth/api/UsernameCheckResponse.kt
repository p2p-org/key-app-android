package com.p2p.wallet.auth.api

import com.google.gson.annotations.SerializedName

data class UsernameCheckResponse(
    @SerializedName("parent_name") val parentName: String,
    @SerializedName("owner") val owner: String,
    @SerializedName("class") val clazz: String,
)