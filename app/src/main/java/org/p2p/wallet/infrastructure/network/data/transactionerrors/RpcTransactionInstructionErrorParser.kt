package org.p2p.wallet.infrastructure.network.data.transactionerrors

import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import org.p2p.core.utils.nextObject

class RpcTransactionInstructionErrorParser {
    /**
     *    [
     *        3,
     *        { "Custom": 6022 }
     *    ]
     */
    fun parse(array: JsonReader): RpcTransactionError.InstructionError {
        val programIdError = array.nextInt()

        val instructionErrorType = when (val element = array.peek()) {
            JsonToken.STRING -> {
                parsePrimitiveInstructionError(array.nextString())
            }
            JsonToken.BEGIN_OBJECT -> {
                array.nextObject(::parseObjectInstructionError)
            }
            else -> {
                TransactionInstructionError.Unknown(
                    "Failed to parse instruction error type for $element in ${array.path}"
                )
            }
        }
        return RpcTransactionError.InstructionError(
            instructionIndex = programIdError,
            instructionErrorType = instructionErrorType
        )
    }

    /**
     * "GenericError"
     * "InvalidArgument"
     */
    private fun parsePrimitiveInstructionError(errorTypeName: String): TransactionInstructionError {
        return TransactionInstructionError::class.nestedClasses
            .firstOrNull { it.simpleName == errorTypeName }
            ?.objectInstance as? TransactionInstructionError
            ?: TransactionInstructionError.Unknown(errorTypeName)
    }

    /**
     * { "Custom": 6022 }
     * { "BorshIoError": "SomeString" }
     */
    private fun parseObjectInstructionError(jsonObject: JsonReader): TransactionInstructionError {
        return when (jsonObject.nextName()) {
            "Custom" -> TransactionInstructionError.Custom(jsonObject.nextLong())
            "BorshIoError" -> TransactionInstructionError.BorshIoError(jsonObject.nextString())
            else -> TransactionInstructionError.Unknown(jsonObject.nextString())
        }
    }
}
