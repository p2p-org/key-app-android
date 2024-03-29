package org.p2p.wallet.auth.gateway.api.response

import com.google.gson.annotations.SerializedName

class GatewayServiceResponse<SuccessBody>(
    @SerializedName("result")
    val result: SuccessBody? = null,

    @SerializedName("error")
    val errorBody: GatewayServiceErrorResponse? = null
)

data class GatewayServiceStandardResponse(
    @SerializedName("status")
    val isSuccessful: Boolean
)

data class GatewayServiceErrorResponse(
    @SerializedName("code")
    val errorCode: Int,
    @SerializedName("message")
    val errorMessage: String,
    @SerializedName("data")
    val data: GatewayServiceErrorDataResponse?
)
