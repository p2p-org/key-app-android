package org.p2p.wallet.transaction

import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.notification.ErrorTransactionNotification
import org.p2p.wallet.notification.SwapTransactionNotification
import org.p2p.wallet.rpc.repository.history.RpcHistoryRepository
import org.p2p.wallet.transaction.model.AppTransaction
import org.p2p.wallet.transaction.model.TransactionExecutionState
import timber.log.Timber

class TransactionSendExecutor(private val transaction: AppTransaction) : TransactionExecutor, KoinComponent {

    companion object {
        private const val TAG = "SendTransactionExecutor"
    }

    init {
        Timber
            .tag(TAG)
            .d("New executor created: ${transaction.serializedTransaction}")
    }

    private val rpcRepository: RpcHistoryRepository by inject()
    private val appNotificationManager: AppNotificationManager by inject()

    private val currentState = MutableStateFlow<TransactionExecutionState>(TransactionExecutionState.Idle)

    override suspend fun execute() {
        if (currentState.value !is TransactionExecutionState.Idle) return
        val serializedTransaction = transaction.serializedTransaction
        val isSimulation = transaction.isSimulation

        Timber
            .tag(TAG)
            .d("Transaction execution started: $serializedTransaction")
        try {
            currentState.emit(TransactionExecutionState.Executing(transaction.serializedTransaction))
            val signature = if (isSimulation) {
                rpcRepository.simulateTransaction(serializedTransaction)
            } else {
                rpcRepository.sendTransaction(serializedTransaction)
            }

            Timber.tag(TAG).d("Transaction execution completed: ${transaction.serializedTransaction}")
            currentState.emit(TransactionExecutionState.Finished(transaction.serializedTransaction, signature))
            showNotificationIfNeeded(signature)
        } catch (e: Throwable) {
            Timber.tag(TAG).e(e, "Error sending transaction in background")
            currentState.emit(TransactionExecutionState.Failed(transaction.serializedTransaction, e))
            showErrorNotificationIfNeeded(e.message ?: e.localizedMessage)
        }
    }

    override fun getTransactionId(): String = transaction.serializedTransaction

    override fun getStateFlow(): MutableStateFlow<TransactionExecutionState> = currentState

    private fun showNotificationIfNeeded(signature: String) {
        Timber.tag(TAG).d("Showing in app notification about completed transaction")

        val notification = SwapTransactionNotification(
            signature = signature,
            sourceSymbol = transaction.sourceSymbol,
            destinationSymbol = transaction.destinationSymbol
        )
        appNotificationManager.showSwapTransactionNotification(notification)
    }

    private fun showErrorNotificationIfNeeded(message: String) {
        Timber.tag(TAG).d("Showing in app notification about failed transaction")

        val notification = ErrorTransactionNotification(
            message = message,
            sourceSymbol = transaction.sourceSymbol,
            destinationSymbol = transaction.destinationSymbol
        )
        appNotificationManager.showErrorTransactionNotification(notification)
    }
}
