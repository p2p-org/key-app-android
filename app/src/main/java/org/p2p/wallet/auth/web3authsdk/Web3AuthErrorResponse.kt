package org.p2p.wallet.auth.web3authsdk

import com.google.gson.annotations.SerializedName
import java.lang.Error

data class Web3AuthErrorResponse(
    @SerializedName("name") val errorName: String,
    @SerializedName("code") val errorCode: Int,
    @SerializedName("message") val errorMessage: String,
    @SerializedName("stack") var stack: String?,
) : Error(errorMessage)
