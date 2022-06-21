package org.p2p.wallet.history.strategy

import org.p2p.solanaj.kits.transaction.network.ConfirmedTransactionRootResponse

interface TransactionParsingContext {

    suspend fun parseTransaction(
        root: ConfirmedTransactionRootResponse
    ): ParsingResult

    fun canParse(transaction: ConfirmedTransactionRootResponse): Boolean
}
