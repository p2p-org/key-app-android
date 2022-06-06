package org.p2p.wallet.history.strategy.context

import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse
import org.p2p.wallet.history.strategy.ParsingResult
import org.p2p.wallet.history.strategy.TransactionParsingContext

class AllTransactionParsingContext(
    private val contexts: List<TransactionParsingContext>
) : TransactionParsingContext {

    override fun parseTransaction(
        root: ConfirmedTransactionRootResponse
    ): ParsingResult {
        val parsedTransactions = contexts.filter { it.canParse(root) }.map { parsingContext ->
            parsingContext.parseTransaction(root)
        }
        val successParsedTransactions = parsedTransactions.filterIsInstance<ParsingResult.Transaction>()
        return successParsedTransactions[0]
    }

    override fun canParse(transaction: ConfirmedTransactionRootResponse): Boolean {
        return true
    }
}
