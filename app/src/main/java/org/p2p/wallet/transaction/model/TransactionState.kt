package org.p2p.wallet.transaction.model

import androidx.annotation.StringRes
import org.p2p.wallet.R
import org.p2p.wallet.history.model.HistoryTransaction

sealed class TransactionState {

    data class Progress(
        @StringRes val message: Int = R.string.send_transaction_being_processed
    ) : TransactionState()

    data class SendSuccess(
        val transaction: HistoryTransaction,
        val sourceTokenSymbol: String
    ) : TransactionState()

    data class SwapSuccess(
        val transaction: HistoryTransaction,
        val fromToken: String,
        val toToken: String
    ) : TransactionState()

    data class Error(
        val transaction: HistoryTransaction,
        val message: String
    ) : TransactionState()
}
