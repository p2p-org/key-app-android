package org.p2p.wallet.feerelayer.model

import android.content.res.Resources
import java.io.IOException
import org.p2p.wallet.infrastructure.network.data.ErrorCode

class FeeRelayerException(
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
