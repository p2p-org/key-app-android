package org.p2p.wallet.history.repository.local

import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.utils.Base58String

interface PendingTransactionsLocalRepository {
    suspend fun removePendingTransaction(txSignature: String)
    suspend fun findPendingTransaction(txSignature: String): HistoryTransaction?
    suspend fun getAllPendingTransactions(mintAddress: Base58String): List<HistoryTransaction>
    suspend fun savePendingTransaction(mintAddress: Base58String, txSignature: String, transaction: HistoryTransaction)
}
