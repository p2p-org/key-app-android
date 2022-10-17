package org.p2p.wallet.auth.username.api.response

import com.google.gson.annotations.SerializedName

class RegisterUsernameServiceListResponse<SuccessBody>(
    @SerializedName("result")
    val result: List<SuccessBody>? = null,

    @SerializedName("error")
    val errorBody: RegisterUsernameServiceErrorResponse? = null
)
