package org.p2p.wallet.infrastructure.network.data

import com.google.gson.annotations.SerializedName
import org.p2p.wallet.R

private const val DEFAULT_MESSAGE_RES = R.string.error_general_message

enum class ErrorCode(val messageRes: Int = DEFAULT_MESSAGE_RES) {
    @SerializedName("-32602")
    INVALID_TRANSACTION(R.string.error_invalid_transaction),

    @SerializedName("-32002")
    TRANSACTION_SIMULATION_FAILED(R.string.error_invalid_transaction),

    @SerializedName("404")
    BAD_REQUEST,

    @SerializedName("500")
    SERVER_ERROR;

    val hasSpecifiedMessage = messageRes != DEFAULT_MESSAGE_RES
}
