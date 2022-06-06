package org.p2p.wallet.history.strategy

import org.p2p.solanaj.kits.transaction.TransactionDetails

sealed class ParsingResult {
    data class Transaction(val items: List<TransactionDetails>) : ParsingResult() {
        companion object {
            fun create(vararg items: TransactionDetails) = Transaction(items = items.toList())
        }
    }

    data class Error(val error: Throwable) : ParsingResult()
}
