package org.p2p.wallet.transaction

import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.wallet.infrastructure.navigation.NavigationScreenTracker
import org.p2p.wallet.notification.AppNotificationManager
import org.p2p.wallet.notification.ErrorTransactionNotification
import org.p2p.wallet.notification.SwapTransactionNotification
import org.p2p.wallet.rpc.repository.RpcRepository
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.transaction.model.AppTransaction
import org.p2p.wallet.transaction.model.TransactionExecutionState
import timber.log.Timber

class SendTransactionExecutor(private val transaction: AppTransaction) : TransactionExecutor, KoinComponent {

    companion object {
        private const val TAG = "SendTransactionExecutor"
    }

    private val rpcRepository: RpcRepository by inject()
    private val appNotificationManager: AppNotificationManager by inject()
    private val screenTracker: NavigationScreenTracker by inject()

    private val currentState = MutableStateFlow<TransactionExecutionState>(TransactionExecutionState.Idle)

    override suspend fun execute() {
        val serializedTransaction = transaction.serializedTransaction
        val isSimulation = transaction.isSimulation
        try {
            currentState.emit(TransactionExecutionState.Executing(transaction.transactionId))
            val signature = if (isSimulation) {
                rpcRepository.simulateTransaction(serializedTransaction)
            } else {
                rpcRepository.sendTransaction(serializedTransaction)
            }

            Timber.tag(TAG).d("Transaction execution completed: ${transaction.transactionId}")
            currentState.emit(TransactionExecutionState.Finished(transaction.transactionId, signature))
            showNotificationIfNeeded(signature)
        } catch (e: Throwable) {
            Timber.tag(TAG).e("Error sending transaction in background")
            currentState.emit(TransactionExecutionState.Failed(transaction.transactionId, e))
            showErrorNotificationIfNeeded(e.message ?: e.localizedMessage)
        }
    }

    override fun getTransactionId(): String = transaction.transactionId

    override fun getStateFlow(): MutableStateFlow<TransactionExecutionState> = currentState

    private fun showNotificationIfNeeded(signature: String) {
        val swapScreenName = OrcaSwapFragment::javaClass.name
        /*
        * Show notification only if user left the swap screen
        * */
        val currentScreen = screenTracker.getCurrentScreen()
        Timber.tag(TAG).d("Going to compare $swapScreenName with current screen $currentScreen")
        if (currentScreen == swapScreenName) return

        Timber.tag(TAG).d("Showing in app notification about completed transaction")

        val notification = SwapTransactionNotification(
            signature = signature,
            sourceSymbol = transaction.sourceSymbol,
            destinationSymbol = transaction.destinationSymbol
        )
        appNotificationManager.showSwapTransactionNotification(notification)
    }

    private fun showErrorNotificationIfNeeded(message: String) {
        val swapScreenName = OrcaSwapFragment::javaClass.name
        /*
        * Show notification only if user left the swap screen
        * */
        val currentScreen = screenTracker.getCurrentScreen()
        Timber.tag(TAG).d("Going to compare $swapScreenName with current screen $currentScreen")
        if (currentScreen == swapScreenName) return

        Timber.tag(TAG).d("Showing in app notification about failed transaction")

        val notification = ErrorTransactionNotification(
            message = message,
            sourceSymbol = transaction.sourceSymbol,
            destinationSymbol = transaction.destinationSymbol
        )
        appNotificationManager.showErrorTransactionNotification(notification)
    }
}