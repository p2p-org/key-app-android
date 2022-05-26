package org.p2p.wallet.transaction.model

import androidx.annotation.StringRes
import org.p2p.solanaj.kits.transaction.TransactionDetails
import org.p2p.solanaj.model.types.ConfirmationStatus
import org.p2p.wallet.R

enum class TransactionStatus(@StringRes val resValue: Int) {
    COMPLETED(R.string.details_completed),
    PENDING(R.string.details_pending),
    ERROR(R.string.details_error);

    companion object {
        fun from(response: TransactionDetails): TransactionStatus {
            return when {
                response.status == ConfirmationStatus.CONFIRMED -> PENDING
                response.error.isNullOrEmpty() -> COMPLETED
                else -> ERROR
            }
        }
    }
}
