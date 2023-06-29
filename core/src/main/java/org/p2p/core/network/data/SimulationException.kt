package org.p2p.core.network.data

import android.content.res.Resources
import java.io.IOException

class SimulationException(
    val errorCode: ErrorCode,
    fullMessage: String,
    private val errorMessage: String?
) : IOException("statusCode: $errorCode, errorMessage: $fullMessage") {

    fun getErrorMessage(resources: Resources) = if (errorCode.hasSpecifiedMessage) {
        resources.getString(errorCode.messageRes)
    } else {
        errorMessage
    }

    fun getDirectMessage(): String? = errorMessage
}
