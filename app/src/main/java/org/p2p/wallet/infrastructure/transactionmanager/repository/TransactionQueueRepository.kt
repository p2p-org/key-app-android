package org.p2p.wallet.infrastructure.transactionmanager.repository

import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.transaction.model.AppTransaction
import org.p2p.wallet.transaction.model.TransactionExecutionState
import org.p2p.wallet.transaction.model.progressstate.TransactionState

interface TransactionQueueRepository {

    fun addInQueue(transaction: AppTransaction)
    fun replaceTransactionsInQueue(transactions: List<AppTransaction>)
    fun getTransactionById(transactionId: String): AppTransaction?

    suspend fun emit(transactionId: String, state: TransactionExecutionState)
    fun getExecutionStateByTransactionId(transactionId: String): Flow<TransactionExecutionState>

    suspend fun emit(transactionId: String, transactionState: TransactionState)
    fun getTransactionStateById(transactionId: String): Flow<TransactionState>

    fun getAllPendingTransactions(): List<AppTransaction>
}
