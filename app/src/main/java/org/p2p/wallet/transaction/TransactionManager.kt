package org.p2p.wallet.transaction

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.p2p.wallet.common.di.AppScope
import org.p2p.wallet.transaction.model.AppTransaction
import org.p2p.wallet.transaction.model.TransactionExecutionState
import org.p2p.wallet.transaction.model.TransactionState
import timber.log.Timber
import java.util.concurrent.Executors

/**
 * This manager is responsible for sending transaction despite what the user is doing during the process
 * Each transaction added in queue is being executed immediately in separate coroutine
 * */
class TransactionManager private constructor(
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

    /*
   * This one is to show user transaction id when progress dialog is shown
   * */
    private val transactionStateFlow = MutableStateFlow<TransactionState>(TransactionState.Progress())

    private val pendingTransactions = mutableListOf<AppTransaction>()

    private val executors = HashSet<TransactionExecutor>()

    fun addInQueue(transaction: AppTransaction) {
        addInQueue(listOf(transaction))
    }

    @Synchronized
    fun addInQueue(transactions: List<AppTransaction>) {
        /*
         * Checking if transaction is already added
         * */
        Timber.tag(TAG).w("Adding new transactions to the queue")
        pendingTransactions.clear()
        pendingTransactions.addAll(transactions)
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

    fun getTransactionStateFlow(): Flow<TransactionState> = transactionStateFlow

    fun getTransactionState(): TransactionState = transactionStateFlow.value

    suspend fun emitTransactionState(state: TransactionState) {
        transactionStateFlow.emit(state)
    }

    private fun executeTransactions() {
        pendingTransactions.forEach {
            executors.add(TransactionSendExecutor(it))
        }

        Timber.tag(TAG).d("Starting execution, executors count: ${pendingTransactions.size}")

        /*
        * Each transaction is being executed in separate coroutine
        * */
        executors.forEach {
            launch { it.execute() }
        }
    }
}
