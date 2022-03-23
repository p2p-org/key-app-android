package org.p2p.wallet.transaction.model

import androidx.annotation.StringRes
import org.p2p.wallet.R

enum class TransactionStatus(@StringRes val resValue: Int) {
    COMPLETED(R.string.details_completed),
    PENDING(R.string.details_pending),
    ERROR(R.string.details_error);
}
