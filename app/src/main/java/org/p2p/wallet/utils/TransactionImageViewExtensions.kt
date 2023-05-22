package org.p2p.wallet.utils

import org.p2p.uikit.R
import org.p2p.wallet.transaction.model.HistoryTransactionStatus

fun HistoryTransactionStatus?.getStatusIcon(): Int? = when (this) {
    HistoryTransactionStatus.PENDING -> R.drawable.ic_state_pending
    HistoryTransactionStatus.ERROR -> R.drawable.ic_state_error
    else -> null
}
