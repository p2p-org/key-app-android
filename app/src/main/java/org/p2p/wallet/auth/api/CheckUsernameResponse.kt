package org.p2p.wallet.auth.api

import com.google.gson.annotations.SerializedName

data class CheckUsernameResponse(
    @SerializedName("parent_name")
    val parentName: String,
    @SerializedName("owner")
    val owner: String,
    @SerializedName("class")
    val clazz: String,
)
