package org.p2p.wallet.transaction.model.progressstate

import androidx.annotation.StringRes
import org.p2p.wallet.R

sealed class TransactionState {

    open class Progress(
        @StringRes val message: Int = R.string.send_transaction_being_processed,
        @StringRes val description: Int = R.string.transaction_description_progress,
    ) : TransactionState()

    open class Success : TransactionState()

    open class Error(
        open val message: String,
    ) : TransactionState()
}
