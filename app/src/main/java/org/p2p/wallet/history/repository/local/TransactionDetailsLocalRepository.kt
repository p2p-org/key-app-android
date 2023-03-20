package org.p2p.wallet.history.repository.local

import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.wallet.history.model.HistoryTransaction

interface TransactionDetailsLocalRepository {
    suspend fun saveTransactions(transactionDetails: List<TransactionDetails>)
    suspend fun getTransactions(signatures: List<String>): List<TransactionDetails>
    suspend fun savePendingTransaction(txSignature: String, transaction: HistoryTransaction)
    suspend fun getAllPendingTransactions(): List<HistoryTransaction>
    suspend fun findPendingTransaction(txSignature: String): HistoryTransaction?
    suspend fun removePendingTransaction(txSignature: String)
    suspend fun deleteAll()
}
