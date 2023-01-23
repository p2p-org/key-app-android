package org.p2p.wallet.infrastructure.sell

interface HiddenSellTransactionsStorageContract {
    fun putTransaction(transactionId: String)
    fun isTransactionHidden(transactionId: String): Boolean
}
