package com.p2p.wallet.infrastructure.network

import android.content.Context
import java.io.IOException

class ServerException(
    private val errorCode: ErrorCode,
    private val errorMessage: String?
) : IOException("statusCode: $errorCode, errorMessage: $errorMessage") {

    fun getErrorMessage(context: Context) = if (errorCode.hasSpecifiedMessage) {
        context.getString(errorCode.messageRes)
    } else {
        errorMessage
    }
}