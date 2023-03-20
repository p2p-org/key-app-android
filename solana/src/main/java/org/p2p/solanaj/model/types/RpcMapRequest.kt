package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class RpcMapRequest(
    @SerializedName("method")
    val method: String,

    @SerializedName("params")
    val params: Map<String, Any?>,

    @SerializedName("jsonrpc")
    val jsonrpc: String = "2.0",

    @SerializedName("id")
    val id: String = UUID.randomUUID().toString()
) {

    constructor(method: String, params: Map<String, Any?>) : this(
        method = method, params = params, jsonrpc = "2.0"
    )
}
