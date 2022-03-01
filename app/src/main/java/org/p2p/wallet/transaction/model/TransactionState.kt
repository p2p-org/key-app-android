package org.p2p.wallet.transaction.model

import androidx.annotation.StringRes
import org.p2p.wallet.R

sealed class TransactionState(open val transactionId: String) {

    data class Progress(
        override val transactionId: String = "...",
        @StringRes val message: Int = R.string.send_transaction_being_processed
    ) : TransactionState(transactionId)

    data class SendSuccess(
        override val transactionId: String,
        val sourceTokenSymbol: String
    ) : TransactionState(transactionId)

    data class SwapSuccess(
        override val transactionId: String,
        val fromToken: String,
        val toToken: String
    ) : TransactionState(transactionId)

    data class Error(
        override val transactionId: String,
        val message: String
    ) : TransactionState(transactionId)
}