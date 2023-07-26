package org.p2p.wallet.transaction.model.progressstate

import org.p2p.wallet.history.model.HistoryTransaction

sealed class SendSwapProgressState : TransactionState() {

    data class Success(
        val transaction: HistoryTransaction,
        val sourceTokenSymbol: String,
        val destinationTokenSymbol: String? = null
    ) : TransactionState.Success()

    data class Error(
        override val message: String,
        val transaction: HistoryTransaction? = null,
    ) : TransactionState.Error(message)
}
