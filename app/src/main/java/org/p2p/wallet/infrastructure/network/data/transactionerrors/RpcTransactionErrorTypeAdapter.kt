package org.p2p.wallet.infrastructure.network.data.transactionerrors

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import timber.log.Timber
import org.p2p.core.utils.nextArray
import org.p2p.core.utils.nextObject

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
class RpcTransactionErrorTypeAdapter(
    private val instructionErrorParser: RpcTransactionInstructionErrorParser
) : TypeAdapter<RpcTransactionError>() {

    override fun read(reader: JsonReader): RpcTransactionError {
        return try {
            when (val errKeyType = reader.peek()) {
                // : "AccountInUse"
                JsonToken.STRING -> createPrimitiveError(reader.nextString())
                // : { "DuplicateInstruction": [ 3 ] }
                JsonToken.BEGIN_OBJECT -> reader.nextObject(::parseObjectError)
                else -> RpcTransactionError.Unknown("Failed to parse error for $errKeyType in ${reader.path}")
            }
        } catch (parsingFatalError: Throwable) {
            logParsingError(parsingFatalError, reader)
            RpcTransactionError.Unknown("Failed to parse error in ${reader.path}")
        }
    }

    private fun parseObjectError(jsonObject: JsonReader): RpcTransactionError {
        return when (val name = jsonObject.nextName()) {
            "InstructionError" -> {
                jsonObject.nextArray { array ->
                    instructionErrorParser.parse(array)
                }
            }
            "DuplicateInstruction" -> {
                jsonObject.nextArray { array ->
                    RpcTransactionError.DuplicateInstruction(array.nextInt())
                }
            }
            "InsufficientFundsForRent" -> {
                jsonObject.nextObject { obj ->
                    obj.nextName() // skipping
                    RpcTransactionError.InsufficientFundsForRent(obj.nextInt())
                }
            }
            else -> {
                RpcTransactionError.Unknown("Failed to parse error for $name in ${jsonObject.path}")
            }
        }
    }

    private fun createPrimitiveError(value: String): RpcTransactionError {
        return RpcTransactionError::class.nestedClasses
            .firstOrNull { it.simpleName == value }
            ?.objectInstance as? RpcTransactionError
            ?: RpcTransactionError.Unknown(value)
    }

    private fun logParsingError(error: Throwable, originalReader: JsonReader) {
        val wholeJson = runCatching { Gson().fromJson<String>(originalReader, String::class.java) }
            .getOrDefault("parsing failed")
        Timber.tag("RpcTransactionErrorTypeAdapter").i(wholeJson)
        Timber.tag("RpcTransactionErrorTypeAdapter").e(
            TransactionErrorParseFailed("Encountered fatal error", error)
        )
    }

    override fun write(out: JsonWriter, value: RpcTransactionError?) {
        error("No writing implemented, no need for now")
    }
}
