package org.p2p.wallet.infrastructure.transactionmanager

import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.transaction.model.AppTransaction
import org.p2p.wallet.transaction.model.progressstate.TransactionState

interface TransactionManager {
    fun addInQueue(transaction: AppTransaction)
    fun addInQueue(transactions: List<AppTransaction>)
    fun getTransactionStateFlow(transactionId: String): Flow<TransactionState>
    suspend fun emitTransactionState(transactionId: String, state: TransactionState)
    fun executeTransactions()
}
