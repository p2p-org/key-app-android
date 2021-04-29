package com.p2p.wallet.infrastructure.network

import java.io.IOException

class ServerException(
    errorCode: String,
    errorDescription: String?,
    errorMessage: String?
) : IOException("statusCode: $errorCode, errorMessage: $errorMessage") {

    private val description: String? = errorDescription ?: errorMessage

    fun getErrorDescription() = description
}