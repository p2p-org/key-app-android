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

    data class ClaimSuccess(
        val bundleId: String,
        val sourceTokenSymbol: String
    ) : TransactionState()

    data class SwapSuccess(
        val transaction: HistoryTransaction,
        val fromToken: String,
        val toToken: String
    ) : TransactionState()

    object JupiterSwapSuccess : TransactionState()

    data class JupiterSwapFailed(
        val failure: TransactionStateSwapFailureReason
    ) : TransactionState()

    data class Error(
        val message: String,
        val transaction: HistoryTransaction? = null,
    ) : TransactionState()
}
