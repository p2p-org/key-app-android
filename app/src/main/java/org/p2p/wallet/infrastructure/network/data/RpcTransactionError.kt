package org.p2p.wallet.infrastructure.network.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import org.p2p.wallet.utils.getOrNull

/**
 * For parsing structs like:
 * ```json
 * "err": {
 *    "InstructionError": [
 *        3,
 *        { "Custom": 6022 }
 *    ]
 * }
 *
 *    or
 * "err": {
 *    "InstructionError": [
 *        3,
 *        "AccountInUse"
 *    ]
 * }
 * ```
 * @return valid typed RpcTransactionError or null if parsing is failed
 */
sealed interface RpcTransactionError {
    companion object {
        fun from(errJson: JsonObject): RpcTransactionError? {
            // for example "InstructionError"
            val transactionErrorType: String? = errJson.keySet().firstOrNull()
            return when (transactionErrorType) {
                "AccountInUse" -> AccountInUse
                "InstructionError" -> parseInstructionError(errJson, transactionErrorType)
                else -> null
            }
        }
    }

    object AccountInUse : RpcTransactionError

    class InstructionError(
        val instructionIndex: Int,
        val errorType: InstructionErrorType
    ) : RpcTransactionError
}

/**
 *  [3, { "Custom": 6022 }]
 *  [3, "InvalidStuff"]
 */
fun RpcTransactionError.Companion.parseInstructionError(
    errJson: JsonObject,
    transactionErrorType: String
): RpcTransactionError.InstructionError? {
    val instructionErrorArray: JsonArray = errJson.getAsJsonArray(transactionErrorType)
    val programIndex = instructionErrorArray.firstOrNull()?.asInt ?: return null

    val instructionErrorElement = instructionErrorArray.getOrNull(1) ?: return null
    return RpcTransactionError.InstructionError(
        instructionIndex = programIndex,
        errorType = InstructionErrorType.from(instructionErrorElement)
    )
}
