package org.p2p.wallet.utils

import org.p2p.uikit.R
import org.p2p.uikit.atoms.TransactionSwapImageView
import org.p2p.uikit.atoms.UiKitTransactionImageView
import org.p2p.wallet.transaction.model.HistoryTransactionStatus

fun UiKitTransactionImageView.setStatus(status: HistoryTransactionStatus?) {
    setStatusIcon(status.getStatusIcon())
}

fun TransactionSwapImageView.setStatus(status: HistoryTransactionStatus?) {
    setStatusIcon(status.getStatusIcon())
}

fun HistoryTransactionStatus?.getStatusIcon(): Int? = when (this) {
    HistoryTransactionStatus.PENDING -> R.drawable.ic_state_pending
    HistoryTransactionStatus.ERROR -> R.drawable.ic_state_error
    else -> null
}
