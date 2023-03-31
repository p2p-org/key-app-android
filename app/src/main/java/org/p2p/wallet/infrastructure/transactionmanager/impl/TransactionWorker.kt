package org.p2p.wallet.infrastructure.transactionmanager.impl

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.p2p.wallet.infrastructure.transactionmanager.repository.TransactionQueueRepository
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.notification.ErrorTransactionNotification
import org.p2p.wallet.notification.SwapTransactionNotification
import org.p2p.wallet.rpc.repository.history.RpcTransactionRepository
import org.p2p.wallet.transaction.model.TransactionExecutionState
import timber.log.Timber

class TransactionWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val rpcRepository: RpcTransactionRepository,
    private val appNotificationManager: AppNotificationManager,
    private val transactionQueueRepository: TransactionQueueRepository
) : CoroutineWorker(context, workerParams), KoinComponent {

    private val transactionId = inputData.getString(KEY_TRANSACTION_ID).orEmpty()
    private val transaction = transactionQueueRepository.getTransactionById(transactionId)

    companion object {
        private const val TAG = "Transaction Worker"
        const val KEY_TRANSACTION_ID = "TRANSACTION_ID"
    }

    init {
        Timber.tag(TAG)
            .d("New worker created: ${transaction?.serializedTransaction}")
    }

    override suspend fun doWork(): Result {
        if (transaction == null) {
            return Result.failure()
        }
        val serializedTransaction = transaction.serializedTransaction
        val isSimulation = transaction.isSimulation
        Timber
            .tag(TAG)
            .d("Transaction worker started: $serializedTransaction")

        try {
            transactionQueueRepository.emit(
                transactionId = transactionId,
                state = TransactionExecutionState.Executing(transaction.serializedTransaction)
            )
            val signature = if (isSimulation) {
                rpcRepository.simulateTransaction(serializedTransaction)
            } else {
                rpcRepository.sendTransaction(serializedTransaction)
            }

            Timber.tag(TAG).d("Transaction worker completed: ${transaction.serializedTransaction}")
            transactionQueueRepository.emit(
                transactionId = transactionId,
                state = TransactionExecutionState.Finished(transaction.serializedTransaction, signature)
            )
            showNotificationIfNeeded(signature)
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Error sending transaction in background")
            transactionQueueRepository.emit(
                transactionId = transactionId,
                state = TransactionExecutionState.Failed(transaction.serializedTransaction, e)
            )
            showErrorNotificationIfNeeded(e.message ?: e.localizedMessage)
        }
        return Result.success()
    }

    private fun showNotificationIfNeeded(signature: String) {
        Timber.tag(TAG).d("Showing in app notification about completed transaction")

        if (transaction == null) {
            return
        }
        val notification = SwapTransactionNotification(
            signature = signature,
            sourceSymbol = transaction.sourceSymbol,
            destinationSymbol = transaction.destinationSymbol
        )
        appNotificationManager.showSwapTransactionNotification(notification)
    }

    private fun showErrorNotificationIfNeeded(message: String) {
        Timber.tag(TAG).d("Showing in app notification about failed transaction")

        if (transaction == null) {
            return
        }
        val notification = ErrorTransactionNotification(
            message = message,
            sourceSymbol = transaction.sourceSymbol,
            destinationSymbol = transaction.destinationSymbol
        )
        appNotificationManager.showErrorTransactionNotification(notification)
    }
}
