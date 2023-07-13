package org.p2p.wallet.history.repository.remote

import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.history.repository.local.PendingTransactionsLocalRepository

/**
 * On each remote fetch we check for completed pending transactions and remove them from app
 */
class HistoryPendingTransactionsCleaner(
    private val pendingTransactionsLocalRepository: PendingTransactionsLocalRepository,
) {

    suspend fun removeCompletedPendingTransactions(
        pendingTransactions: List<HistoryTransaction>,
        newTransactions: List<HistoryTransaction>
    ) {
        pendingTransactions.forEach { pendingTransaction ->
            checkCompletedRpcPendingTransactions(pendingTransaction, newTransactions)

            if (pendingTransaction.isStrigaClaim()) {
                pendingTransaction as RpcHistoryTransaction.Transfer
                checkCompletedStrigaPendingTransactions(pendingTransaction, newTransactions)
            }
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
        pendingTransaction: RpcHistoryTransaction.Transfer,
        newTransactions: List<HistoryTransaction>
    ) {
        val strigaClaimTransactionsAmounts = newTransactions.filterIsInstance<RpcHistoryTransaction.Transfer>()
            .filter { it.isStrigaClaim() }
            .map { it.amount.total }

        if (pendingTransaction.amount.total in strigaClaimTransactionsAmounts) {
            pendingTransactionsLocalRepository.removePendingTransaction(pendingTransaction.getHistoryTransactionId())
        }
    }

    private fun HistoryTransaction.isStrigaClaim(): Boolean {
        return this is RpcHistoryTransaction.Transfer &&
            senderAddress == RpcHistoryTransaction.Transfer.STRIGA_CLAIM_SENDER_ADDRESS
    }
}
