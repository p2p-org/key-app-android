package org.p2p.wallet.auth.username.api.response

import com.google.gson.annotations.SerializedName

class RegisterUsernameServiceResponse<SuccessBody>(
    @SerializedName("result")
    val result: SuccessBody? = null,

    @SerializedName("error")
    val errorBody: RegisterUsernameServiceErrorResponse? = null
)
