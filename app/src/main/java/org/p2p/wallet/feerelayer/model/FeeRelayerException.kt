package org.p2p.wallet.feerelayer.model

import android.content.res.Resources
import java.io.IOException
import org.p2p.core.network.data.ErrorCode

class FeeRelayerException(
    val errorCode: ErrorCode,
    rawResponseBody: String,
    private val errorMessage: String?
) : IOException("statusCode: $errorCode, errorMessage: $rawResponseBody") {

    fun getErrorMessage(resources: Resources) = if (errorCode.hasSpecifiedMessage) {
        resources.getString(errorCode.messageRes)
    } else {
        errorMessage
    }

    fun getDirectMessage(): String? = errorMessage ?: message
}
