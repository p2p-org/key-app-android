package org.p2p.wallet.history.repository.remote

import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.repository.local.PendingTransactionsLocalRepository
import org.p2p.wallet.striga.wallet.interactor.StrigaWalletInteractor

/**
 * On each remote fetch we check for completed pending transactions and remove them from app
 */
class HistoryPendingTransactionsCleaner(
    private val pendingTransactionsLocalRepository: PendingTransactionsLocalRepository,
    private val strigaWalletInteractor: StrigaWalletInteractor
) {

    suspend fun removeCompletedPendingTransactions(
        pendingTransactions: List<HistoryTransaction>,
        newTransactions: List<HistoryTransaction>
    ) {
        val strigaUserCryptoAddress = kotlin.runCatching { strigaWalletInteractor.getCryptoAccountDetails() }
            .getOrNull()
            ?.depositAddress

        pendingTransactions.forEach { pendingTransaction ->
            checkCompletedRpcPendingTransactions(
                pendingTransaction = pendingTransaction,
                newTransactions = newTransactions
            )
            checkCompletedStrigaPendingTransactions(
                pendingTransaction = pendingTransaction,
                strigaRemoteOnRampTransactions = newTransactions,
                strigaUserCryptoAddress = strigaUserCryptoAddress
            )
        }
    }

    private suspend fun checkCompletedRpcPendingTransactions(
        pendingTransaction: HistoryTransaction,
        newTransactions: List<HistoryTransaction>
    ) {
        val txSignature = pendingTransaction.getHistoryTransactionId()
        val remoteTransactionIds = newTransactions.map(HistoryTransaction::getHistoryTransactionId)
        if (txSignature in remoteTransactionIds) {
            pendingTransactionsLocalRepository.removePendingTransaction(txSignature)
        }
    }

    /**
     * Check the remote transactions that are Striga claim have exactly the same amount as pending transaction
     * if claimed amount exists in remote transaction - then it's not pending anymore
     */
    private suspend fun checkCompletedStrigaPendingTransactions(
        pendingTransaction: HistoryTransaction,
        strigaRemoteOnRampTransactions: List<HistoryTransaction>,
        strigaUserCryptoAddress: String?,
    ) {
        strigaUserCryptoAddress ?: return

        if (pendingTransaction.isStrigaOnRamp(strigaUserCryptoAddress)) {
            pendingTransaction as RpcHistoryTransaction.Transfer
            val onRampTransactionsAmounts = strigaRemoteOnRampTransactions
                .filterIsInstance<RpcHistoryTransaction.Transfer>()
                .filter { it.senderAddress == strigaUserCryptoAddress }
                .map { it.amount.total }

            if (pendingTransaction.amount.total in onRampTransactionsAmounts) {
                pendingTransactionsLocalRepository.removePendingTransaction(
                    txSignature = pendingTransaction.getHistoryTransactionId()
                )
            }
        }
    }

    private fun HistoryTransaction.isStrigaOnRamp(strigaUserCryptoAddress: String): Boolean {
        return this is RpcHistoryTransaction.Transfer && senderAddress == strigaUserCryptoAddress
    }
}
