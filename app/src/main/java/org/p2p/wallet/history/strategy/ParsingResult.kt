package org.p2p.wallet.history.strategy

import org.p2p.solanaj.kits.transaction.TransactionDetails

sealed interface ParsingResult {
    data class Transaction(val details: List<TransactionDetails>) : ParsingResult {
        companion object {
            fun create(vararg details: TransactionDetails) = Transaction(details = details.toList())
        }
    }

    data class Error(val error: Throwable) : ParsingResult
}
