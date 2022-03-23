package org.p2p.solanaj.model.types

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class RpcRequest(
    @SerializedName("method")
    val method: String,

    @SerializedName("params")
    val params: List<Any>?,

    @SerializedName("jsonrpc")
    val jsonrpc: String = "2.0",

    @SerializedName("id")
    val id: String = UUID.randomUUID().toString()
) {

    constructor(method: String, params: List<Any>) : this(
        method = method, params = params, jsonrpc = "2.0"
    )
}
