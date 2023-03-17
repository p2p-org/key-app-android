package org.p2p.wallet.infrastructure.network.data

import android.content.res.Resources
import com.google.gson.JsonObject
import java.io.IOException

class ServerException(
    val errorCode: ErrorCode,
    fullMessage: String,
    private val errorMessage: String?,
    val jsonErrorBody: JsonObject? = null,
    val domainErrorType: RpcError? = null
) : IOException("statusCode: $errorCode, errorMessage: $fullMessage") {

    fun getErrorMessage(resources: Resources) = if (errorCode.hasSpecifiedMessage) {
        resources.getString(errorCode.messageRes)
    } else {
        errorMessage
    }

    fun getDirectMessage(): String? = errorMessage
}
