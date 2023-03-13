package org.p2p.wallet.infrastructure.network.data

import com.google.gson.JsonArray
import com.google.gson.JsonElement
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
 *        "Other"
 *    ]
 * }
 *    or
 * "err": "AccountInUse"
 * ```
 * @see [RpcTransactionErrorParserTest]
 * @return valid typed RpcTransactionError or null if parsing is failed
 */
sealed interface RpcError {
    companion object {
        fun from(errorJson: JsonElement): RpcError? {
            return when {
                errorJson.isJsonObject -> parseComplexError(errorJson.asJsonObject)
                errorJson.isJsonPrimitive -> parseSimpleError(errorJson.asString)
                else -> null
            }
        }

        private fun parseComplexError(json: JsonObject): RpcError? {
            return when (val complexErrorName: String? = json.keySet().firstOrNull()) {
                "InstructionError" -> parseInstructionError(json, complexErrorName)
                "DuplicateInstruction", "InsufficientFundsForRent" -> null // not supported atm
                else -> null
            }
        }

        private fun parseSimpleError(errorName: String): RpcError? {
            return when (errorName) {
                "AccountInUse" -> AccountInUse
                else -> null // other types are not supported atm
            }
        }
    }

    object AccountInUse : RpcError

    class InstructionError(
        val instructionIndex: Int,
        val instructionErrorType: InstructionErrorType
    ) : RpcError
}

/**
 *  [3, { "Custom": 6022 }]
 *  [3, "InvalidStuff"]
 */
private fun RpcError.Companion.parseInstructionError(
    errJson: JsonObject,
    transactionErrorType: String
): RpcError.InstructionError? {
    val instructionErrorArray: JsonArray = errJson.getAsJsonArray(transactionErrorType)
    val programIndex = instructionErrorArray.firstOrNull()?.asInt ?: return null

    val instructionErrorElement = instructionErrorArray.getOrNull(1) ?: return null
    return RpcError.InstructionError(
        instructionIndex = programIndex,
        instructionErrorType = InstructionErrorType.from(instructionErrorElement)
    )
}
