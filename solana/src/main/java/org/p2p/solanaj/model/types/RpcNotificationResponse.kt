package org.p2p.solanaj.model.types

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

data class RpcNotificationResponse(
    @SerializedName("jsonrpc")
    val jsonRpc: String? = null,
    @SerializedName("method")
    val method: String? = null,
    @SerializedName("params")
    val params: JsonObject,
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("result")
    val result: Long? = null
)

data class RpcNotificationResultResponse<T>(
    @SerializedName("value")
    val value: T,
    @SerializedName("subscription")
    val subscription: Int
)
