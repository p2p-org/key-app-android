package org.p2p.wallet.transaction

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.transaction.model.AppTransaction
import org.p2p.wallet.transaction.model.TransactionExecutionState
import timber.log.Timber
import java.util.concurrent.Executors

class TransactionSendManager private constructor(
    appScope: AppScope,
    private val initDispatcher: CoroutineDispatcher
) : CoroutineScope by (appScope + initDispatcher) {

    companion object {
        private const val TAG = "TransactionSendManager"
    }

    constructor(
        appScope: AppScope,
    ) : this(
        appScope,
        Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    )

    private val pendingTransactions = mutableListOf<AppTransaction>()

    private val executors = HashSet<TransactionExecutor>()

    @Synchronized
    fun addInQueue(transaction: AppTransaction) {
        /*
         * Checking if transaction is already added
         * */
        val isAlreadyAdded = pendingTransactions.any { it.transactionId == transaction.transactionId }
        if (isAlreadyAdded) return

        Timber.tag(TAG).w("Adding new transaction to the queue")
        pendingTransactions.add(transaction)
        executeTransactions()
    }

    fun getStateFlow(transactionId: String): Flow<TransactionExecutionState>? {
        val executor = executors.firstOrNull { it.getTransactionId() == transactionId }
        if (executor == null) {
            Timber.tag(TAG).w("There are no transactions are being executed or current id is wrong: $transactionId")
            return null
        }

        return executor.getStateFlow()
    }

    private fun executeTransactions() {
        val filtered = pendingTransactions.filter { transaction ->
            executors.none { it.getTransactionId() == transaction.transactionId }
        }

        filtered.forEach {
            executors.add(SendTransactionExecutor(it))
        }

        Timber.tag(TAG).d("Starting execution, executors count: ${filtered.size}")

        /*
        * Each transaction is being executed in separate coroutine
        * */
        executors.forEach {
            launch { it.execute() }
        }
    }
}