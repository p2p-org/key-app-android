package org.p2p.wallet.auth.common

import com.google.gson.annotations.SerializedName

data class Web3AuthError(
    @SerializedName("name") val errorName: String,
    @SerializedName("code") val errorCode: Int,
    @SerializedName("message") val errorMessage: String,
    @SerializedName("stack") var stack: String?,
)
