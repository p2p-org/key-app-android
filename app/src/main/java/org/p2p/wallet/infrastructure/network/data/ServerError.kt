package org.p2p.wallet.infrastructure.network.data

import com.google.gson.annotations.SerializedName

data class ServerError(
    @SerializedName("error")
    val error: ErrorContent
)

data class ErrorContent(
    @SerializedName("code")
    val code: ErrorCode,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: ErrorData?
)

data class ErrorData(
    @SerializedName("logs")
    val logs: List<String>
) {

    fun getErrorLog(): String? {
        val errorPrefix = "Program log: Error: "
        val errorLog = logs.firstOrNull { it.startsWith(errorPrefix) } ?: return null
        return errorLog.substring(errorPrefix.length, errorLog.length)
    }
}
