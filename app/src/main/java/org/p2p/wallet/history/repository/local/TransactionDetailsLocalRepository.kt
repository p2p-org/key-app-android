package org.p2p.wallet.history.repository.local

import org.p2p.solanaj.kits.transaction.TransactionDetails

interface TransactionDetailsLocalRepository {
    suspend fun saveTransactions(transactionDetails: List<TransactionDetails>)
    suspend fun getTransactions(signatures: List<String>): List<TransactionDetails>
    suspend fun deleteAll()
}
