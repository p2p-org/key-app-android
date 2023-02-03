package org.p2p.wallet.history.strategy.context

import org.p2p.solanaj.kits.transaction.TransactionDetailsType
import org.p2p.solanaj.kits.transaction.UnknownDetails
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.solanaj.kits.transaction.network.meta.InstructionResponse
import org.p2p.wallet.common.di.ServiceScope
import org.p2p.wallet.history.strategy.ParsingResult
import org.p2p.wallet.history.strategy.TransactionParsingContext
import org.p2p.wallet.history.strategy.TransactionParsingStrategy
import org.p2p.wallet.utils.appendBreakLine
import timber.log.Timber
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class SolanaParsingContext(
    private val strategies: List<TransactionParsingStrategy>,
    private val serviceScope: ServiceScope
) : TransactionParsingContext {

    override suspend fun parseTransaction(
        root: ConfirmedTransactionRootResponse
    ): ParsingResult = withContext(serviceScope.coroutineContext) {
        val messageResponse = root.transaction?.message
            ?: return@withContext run {
                Timber.i("Creating unknown details transaction: $root")
                ParsingResult.Transaction.create(
                    UnknownDetails(
                        signature = root.transaction?.getTransactionId()!!,
                        blockTime = root.blockTime,
                        slot = root.slot,
                    )
                )
            }
        val instructions = messageResponse
            .instructions
            .map { async { toParsingResult(root, it) } }
            .awaitAll()

        val details = instructions?.filterIsInstance<ParsingResult.Transaction>()
            ?.flatMap { it.details }
            ?: emptyList()

        return@withContext ParsingResult.Transaction(details)
    }

    private class ParsingContextError(
        customMessage: String,
        root: ConfirmedTransactionRootResponse,
        instruction: InstructionResponse
    ) : Throwable(
        message = buildString {
            append(customMessage)
            appendBreakLine()
            append("programId=${instruction.programId};")
            appendBreakLine()
            append("transactionId=${root.transaction?.getTransactionId()}")
            appendBreakLine()
            append("parsedType=${instruction.parsed?.type}")
        }
    )

    private suspend fun toParsingResult(
        root: ConfirmedTransactionRootResponse,
        instruction: InstructionResponse
    ): ParsingResult {
        val signature = root.transaction?.getTransactionId()
            ?: return ParsingResult.Error(
                ParsingContextError(
                    customMessage = "Signature cannot be null",
                    root = root,
                    instruction = instruction
                )
            )

        val type = TransactionDetailsType.valueOf(instruction.parsed?.type)

        val parsingStrategy = strategies.firstOrNull { it.getType() == type }
            ?: return run {
                val strategies = strategies.map(TransactionParsingStrategy::getType)
                ParsingResult.Error(
                    ParsingContextError(
                        customMessage = "Couldn't find any available strategies for type=$type in $strategies",
                        root = root,
                        instruction = instruction
                    )
                )
            }

        return parsingStrategy.parseTransaction(
            signature = signature,
            instruction = instruction,
            transactionRoot = root
        )
    }

    override fun canParse(transaction: ConfirmedTransactionRootResponse): Boolean {
        // Default parser for all type of transactions
        return true
    }
}
