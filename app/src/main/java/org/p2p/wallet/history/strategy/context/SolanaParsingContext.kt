package org.p2p.wallet.history.strategy.context

import org.p2p.solanaj.kits.transaction.TransactionDetailsType
import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.wallet.history.strategy.ParsingResult
import org.p2p.wallet.history.strategy.TransactionParsingContext
import org.p2p.wallet.history.strategy.TransactionParsingStrategy
import java.lang.IllegalStateException

class SolanaParsingContext(
    private val strategies: List<TransactionParsingStrategy>
) : TransactionParsingContext {

    override fun parseTransaction(
        root: ConfirmedTransactionRootResponse
    ): ParsingResult {
        val instructions = root.transaction?.message?.instructions?.filter { it.parsed != null }?.map { instruction ->
            val signature = root.transaction?.getTransactionId()
                ?: return ParsingResult.Error(IllegalStateException("Signature cannot be null"))

            val type = TransactionDetailsType.valueOf(instruction.parsed?.type)
            val parsingStrategy = strategies.first { it.getType() == type }

            parsingStrategy.parseTransaction(
                signature = signature,
                instruction = instruction,
                transactionRoot = root
            )
        }
        val details = instructions?.filterIsInstance<ParsingResult.Transaction>()?.flatMap { it.items } ?: emptyList()

        return ParsingResult.Transaction(details)
    }

    override fun canParse(transaction: ConfirmedTransactionRootResponse): Boolean {
        // Default parser for all type of transactions
        return true
    }
}
