package org.p2p.wallet.infrastructure.network.feerelayer

import org.p2p.wallet.utils.emptyString
import java.util.Locale

object ErrorConverter {

    private const val programFailed = "Program failed to complete: "
    private const val programError = "Program log: Error: "

    private const val codePrefix = "code: "

    private val errorPrefixes = listOf(
        programFailed,
        programError,
        "Transfer: insufficient lamports " // 19266, need 2039280
    )

    private val escapePrefixes = listOf(
        programFailed,
        programError,
        "Transfer: "
    )

    fun fromFeeRelayer(code: Int, rawError: String): FeeRelayerError {
        val logRegex = Regex("\"(?:Program|Transfer:) [^\"]+\"")
        val matches = logRegex.findAll(rawError)
        val logs = matches.map { match ->
            match.groupValues.map { value ->
                value.replace("\"", "")
            }
        }.toList().flatten()
        val currentLog = logs.findFirstValidLog(errorPrefixes)

        val codeRegex = Regex("$codePrefix-?\\d+")
        val feeRelayerCode = codeRegex.findAll(rawError)
            .map {
                it.groupValues
            }.toList()
            .flatten()
            .firstOrNull()
            .toString()
            .replace(codePrefix, emptyString())
            .toIntOrNull()
        return FeeRelayerError(
            feeRelayerCode ?: code,
            escapeAndCapitalizeFirstCharOfLog(currentLog),
            typeFromLog(currentLog)
        )
    }

    private fun typeFromLog(log: String?): FeeRelayerErrorType {
        return when {
            log?.contains("insufficient funds") == true -> FeeRelayerErrorType.INSUFFICIENT_FUNDS
            log?.contains("insufficient lamports") == true -> FeeRelayerErrorType.INSUFFICIENT_FUNDS
            log?.contains("exceeds desired slippage") == true -> FeeRelayerErrorType.SLIPPAGE_LIMIT
            log?.contains("exceeded maximum number of instructions allowed") == true -> {
                FeeRelayerErrorType.MAXIMUM_NUMBER_OF_INSTRUCTIONS_ALLOWED_EXCEEDED
            }
            else -> FeeRelayerErrorType.UNKNOWN
        }
    }

    private fun List<String>.findFirstValidLog(prefixes: List<String>): String? {
        forEach { log ->
            if (log.containsAnyOf(prefixes)) {
                return log
            }
        }
        return null
    }

    private fun escapeAndCapitalizeFirstCharOfLog(log: String?): String? {
        return log.escapePrefixes(escapePrefixes)?.replaceFirstChar {
            if (it.isLowerCase()) {
                it.titlecase(Locale.getDefault())
            } else {
                it.toString()
            }
        }
    }

    private fun String?.escapePrefixes(prefixes: List<String>): String? {
        var resultMessage = this ?: return null
        prefixes.forEach {
            resultMessage = resultMessage.replace(it, emptyString())
        }
        return resultMessage
    }

    private fun String.containsAnyOf(keywords: List<String>): Boolean {
        for (keyword in keywords) {
            if (contains(keyword)) return true
        }
        return false
    }
}
