package org.p2p.wallet.infrastructure.transactionmanager.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.p2p.wallet.transaction.model.AppTransaction
import org.p2p.wallet.transaction.model.TransactionExecutionState
import org.p2p.wallet.transaction.model.progressstate.TransactionState

/**
 * This class is represent storage for saving and getting transactions
 * Which will be used by [TransactionWorker]
 *
 * Maybe it's sense to use database to store them
 */
class TransactionQueueRepositoryImpl : TransactionQueueRepository {

    private val allTransactions = mutableListOf<AppTransaction>()
    private val executionStateMap = mutableMapOf<String, MutableStateFlow<TransactionExecutionState>>()
    private val transactionStateMap = mutableMapOf<String, MutableStateFlow<TransactionState>>()

    override fun addInQueue(transaction: AppTransaction) {
        allTransactions.add(transaction)
    }

    override fun replaceTransactionsInQueue(transactions: List<AppTransaction>) {
        allTransactions.clear()
        allTransactions.addAll(transactions)
    }

    override fun getTransactionById(transactionId: String): AppTransaction? {
        return allTransactions.firstOrNull { it.serializedTransaction == transactionId }
    }

    override suspend fun emit(transactionId: String, state: TransactionExecutionState) {
        if (executionStateMap[transactionId] == null) {
            executionStateMap[transactionId] = MutableStateFlow(state)
        } else {
            executionStateMap[transactionId]?.emit(state)
        }
    }

    override fun getExecutionStateByTransactionId(transactionId: String): Flow<TransactionExecutionState> {
        if (executionStateMap[transactionId] == null) {
            executionStateMap[transactionId] = MutableStateFlow(TransactionExecutionState.Idle)
        }
        return executionStateMap.getValue(transactionId)
    }

    override suspend fun emit(transactionId: String, transactionState: TransactionState) {
        if (transactionStateMap[transactionId] == null) {
            transactionStateMap[transactionId] = MutableStateFlow(transactionState)
        } else {
            transactionStateMap[transactionId]?.emit(transactionState)
        }
    }

    override fun getTransactionStateById(transactionId: String): Flow<TransactionState> {
        if (transactionStateMap[transactionId] == null) {
            transactionStateMap[transactionId] = MutableStateFlow(TransactionState.Progress())
        }
        return transactionStateMap.getValue(transactionId)
    }

    override fun getAllPendingTransactions(): List<AppTransaction> {
        return allTransactions
    }
}
