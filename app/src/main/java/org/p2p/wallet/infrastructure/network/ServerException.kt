package org.p2p.wallet.infrastructure.network

import android.content.Context
import java.io.IOException

class ServerException(
    val errorCode: ErrorCode,
    private val fullMessage: String,
    private val errorMessage: String?
) : IOException("statusCode: $errorCode, errorMessage: $fullMessage") {

    fun getErrorMessage(context: Context) = if (errorCode.hasSpecifiedMessage) {
        context.getString(errorCode.messageRes)
    } else {
        errorMessage
    }

    fun getDirectMessage(): String = errorMessage ?: fullMessage
}