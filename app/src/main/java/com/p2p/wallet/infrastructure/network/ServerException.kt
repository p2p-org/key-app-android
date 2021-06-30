package com.p2p.wallet.infrastructure.network

import android.content.Context
import java.io.IOException

class ServerException(
    private val errorCode: ErrorCode,
    fullMessage: String,
    private val errorMessage: String?
) : IOException("statusCode: $errorCode, errorMessage: $fullMessage") {

    fun getErrorMessage(context: Context) = if (errorCode.hasSpecifiedMessage) {
        context.getString(errorCode.messageRes)
    } else {
        errorMessage
    }
}