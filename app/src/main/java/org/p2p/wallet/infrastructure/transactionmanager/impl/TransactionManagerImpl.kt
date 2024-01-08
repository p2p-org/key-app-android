package org.p2p.wallet.infrastructure.transactionmanager.impl

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import android.content.Context
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import org.p2p.wallet.infrastructure.transactionmanager.TransactionManager
import org.p2p.wallet.infrastructure.transactionmanager.repository.TransactionQueueRepository
import org.p2p.wallet.transaction.model.AppTransaction
import org.p2p.wallet.transaction.model.progressstate.TransactionState

class TransactionManagerImpl(
    private val context: Context,
    private val transactionQueueRepository: TransactionQueueRepository
) : TransactionManager {

    private val transactionsWorkersIdMap = mutableMapOf<AppTransaction, UUID>()

    override fun addInQueue(transaction: AppTransaction) {
        addInQueue(listOf(transaction))
    }

    override fun addInQueue(transactions: List<AppTransaction>) {
        transactionQueueRepository.replaceTransactionsInQueue(transactions)
        executeTransactions()
    }

    override fun getTransactionStateFlow(transactionId: String): Flow<TransactionState> {
        return transactionQueueRepository.getTransactionStateById(transactionId)
    }

    override suspend fun emitTransactionState(transactionId: String, state: TransactionState) {
        transactionQueueRepository.emit(transactionId, state)
    }

    override fun executeTransactions() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        transactionQueueRepository.getAllPendingTransactions().forEach {
            val worker = OneTimeWorkRequestBuilder<TransactionWorker>()
                .setConstraints(constraints)
                .build()
            transactionsWorkersIdMap[it] = worker.id

            WorkManager.getInstance(context)
                .enqueueUniqueWork(it.serializedTransaction, ExistingWorkPolicy.REPLACE, worker)
        }
    }
}
