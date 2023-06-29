package org.p2p.core.network.data

import com.google.gson.annotations.SerializedName
import org.p2p.core.R

const val INVALID_TRANSACTION_ERROR_CODE = -32602
const val TRANSACTION_SIMULATION_FAILED_ERROR_CODE = -32002

private val DEFAULT_MESSAGE_RES = R.string.error_general_message

enum class ErrorCode(val messageRes: Int = DEFAULT_MESSAGE_RES) {
    @SerializedName("$INVALID_TRANSACTION_ERROR_CODE")
    INVALID_TRANSACTION(R.string.error_invalid_transaction),

    @SerializedName("$TRANSACTION_SIMULATION_FAILED_ERROR_CODE")
    TRANSACTION_SIMULATION_FAILED(R.string.error_invalid_transaction),

    @SerializedName("6")
    SLIPPAGE_LIMIT(R.string.error_slippage_limit),

    @SerializedName("7")
    INSUFFICIENT_FUNDS(R.string.error_insufficient_funds),

    @SerializedName("8")
    INVALID_BLOCKHASH(R.string.error_invalid_blockhash),

    @SerializedName("404")
    BAD_REQUEST,

    @SerializedName("500")
    SERVER_ERROR;

    val hasSpecifiedMessage = messageRes != DEFAULT_MESSAGE_RES
}
