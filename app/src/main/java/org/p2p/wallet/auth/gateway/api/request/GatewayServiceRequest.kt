package org.p2p.wallet.auth.gateway.api.request

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class GatewayServiceRequest<ParamsBody>(
    @SerializedName("params")
    val params: ParamsBody,

    @SerializedName("jsonrpc")
    val jsonRpcVersion: String = "2.0",

    @SerializedName("id")
    val requestId: String = UUID.randomUUID().toString(),

    @SerializedName("method")
    val methodName: GatewayServiceJsonRpcMethod
)
