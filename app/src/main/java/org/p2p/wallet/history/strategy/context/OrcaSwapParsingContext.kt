package org.p2p.wallet.history.strategy.context

import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.wallet.history.strategy.ParsingResult
import org.p2p.wallet.history.strategy.TransactionParsingContext
import org.p2p.wallet.history.strategy.TransactionParsingStrategy
import java.lang.IllegalStateException

const val ZERO_AMOUNT = "0"

class OrcaSwapParsingContext(private val strategy: TransactionParsingStrategy) : TransactionParsingContext {

    private val orcaSwapProgramIds = setOf(
        "DjVE6JNiYqPL2QXyCUUh8rNjHrbz9hXHNYt99MQ59qw1", // swap ocra,
        "9W959DqEETiGZocYWCQPaJ6sBmUzgfxXfqGeTEdp3aQP", // swap ocra v2
        "9qvG1zUp8xF1Bi4m6UdRNby1BAAuaDrUxSpv4CmRRMjL", // main deprecated
        "SwaPpA9LAaLfeLi3a68M4DjnLqgtticKg6CnyNwgAC8", // main deprecated
    )

    override fun canParse(transaction: ConfirmedTransactionRootResponse): Boolean {
        val instructions = transaction.transaction?.message?.instructions

        val orcaSwapInstruction = instructions?.firstOrNull { it.programId in orcaSwapProgramIds }
        return orcaSwapInstruction != null
    }

    override fun parseTransaction(
        transactionRoot: ConfirmedTransactionRootResponse
    ): ParsingResult {

        val instructions =
            transactionRoot.transaction?.message?.instructions?.filter { it.parsed != null }?.map { instruction ->
                val signature = transactionRoot.transaction?.getTransactionId()
                    ?: return ParsingResult.Error(IllegalStateException("Signature cannot be null"))

                strategy.parseTransaction(
                    signature = signature,
                    instruction = instruction,
                    transactionRoot = transactionRoot
                )
            }
        val details = instructions?.filterIsInstance<ParsingResult.Transaction>()?.flatMap { it.items } ?: emptyList()

        return ParsingResult.Transaction(details)
    }
}
