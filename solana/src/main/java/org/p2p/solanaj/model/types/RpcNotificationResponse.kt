package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName

data class RpcNotificationResponse(
    @SerializedName("jsonrpc")
    val jsonRpc: String? = null,
    @SerializedName("method")
    val method: String? = null,
    @SerializedName("params")
    val params: String,
    @SerializedName("subscription")
    val subscription: Long
)
