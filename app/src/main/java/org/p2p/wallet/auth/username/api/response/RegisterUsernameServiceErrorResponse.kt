package org.p2p.wallet.auth.username.api.response

import com.google.gson.annotations.SerializedName

data class RegisterUsernameServiceErrorResponse(
    @SerializedName("code")
    val errorCode: Int,
    @SerializedName("message")
    val errorMessage: String,
)
