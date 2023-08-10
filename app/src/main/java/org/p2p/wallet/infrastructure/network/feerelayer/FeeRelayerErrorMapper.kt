package org.p2p.wallet.infrastructure.network.feerelayer

import timber.log.Timber
import java.util.Locale
import org.p2p.wallet.utils.emptyString

object FeeRelayerErrorMapper {

    private const val programFailed = "Program failed to complete: "
    private const val programError = "Program log: Error: "

    private const val codePrefix = "code: "

    private val errorPrefixes: List<String> = listOf(
        programFailed,
        programError,
        "Transfer: insufficient lamports " // 19266, need 2039280
    )

    private val escapePrefixes: List<String> = listOf(
        programFailed,
        programError,
        "Transfer: "
    )

    fun fromFeeRelayer(code: Int, rawError: String): FeeRelayerError {
        val logRegex = Regex("\"(?:Program|Transfer:) [^\"]+\"")
        val matches = logRegex.findAll(rawError)
        val logs = matches.flatMap { match ->
            match.groupValues.map { it.replace("\"", emptyString()) }
        }.toList()
        val currentLog = logs.findFirstValidLog(errorPrefixes)
        Timber.tag("FeeRelayerErrorMapper").i("Error: $code\n${logs.joinToString("\n")}")

        val codeRegex = Regex("$codePrefix-?\\d+")
        val feeRelayerCode = codeRegex.findAll(rawError)
            .flatMap(MatchResult::groupValues)
            .firstOrNull()
            ?.replace(codePrefix, emptyString())
            ?.toIntOrNull()
        return FeeRelayerError(
            code = feeRelayerCode ?: code,
            message = escapeAndCapitalizeFirstCharOfLog(currentLog),
            type = currentLog?.let(::typeFromLog) ?: typeFromLog(rawError)
        )
    }

    private fun typeFromLog(log: String?): FeeRelayerErrorType {
        return when {
            log?.contains("insufficient funds") == true -> FeeRelayerErrorType.INSUFFICIENT_FUNDS
            log?.contains("insufficient lamports") == true -> FeeRelayerErrorType.INSUFFICIENT_FUNDS
            log?.contains("exceeds desired slippage") == true -> FeeRelayerErrorType.SLIPPAGE_LIMIT
            log?.contains("Invalid blockhash") == true ||
                log?.contains("Blockhash not found") == true -> FeeRelayerErrorType.INVALID_BLOCKHASH
            log?.contains("exceeded maximum number of instructions allowed") == true -> {
                FeeRelayerErrorType.MAXIMUM_NUMBER_OF_INSTRUCTIONS_ALLOWED_EXCEEDED
            }
            else -> FeeRelayerErrorType.UNKNOWN
        }
    }

    private fun List<String>.findFirstValidLog(prefixes: List<String>): String? =
        firstOrNull { it.containsAnyOf(prefixes) }

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

    private fun String.containsAnyOf(keywords: List<String>): Boolean = keywords.any { contains(it) }
}
