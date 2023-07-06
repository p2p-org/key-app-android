package org.p2p.core.network.data

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

data class ServerErrorResponse(
    @SerializedName("error")
    val error: ErrorContentResponse
)

data class ErrorContentResponse(
    @SerializedName("code")
    val code: ErrorCode?,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: ErrorDataResponse?
)

data class ErrorDataResponse(
    @SerializedName("logs")
    val logs: List<String>,
    @SerializedName("err")
    val rpcErrorDetails: JsonElement? = null // can be object or plain string
) {

    fun getErrorLog(): String? {
        val errorPrefix = "Program log: Error: "
        val errorLog = logs.firstOrNull { it.startsWith(errorPrefix) } ?: return null
        return errorLog.substring(errorPrefix.length, errorLog.length)
    }
}
