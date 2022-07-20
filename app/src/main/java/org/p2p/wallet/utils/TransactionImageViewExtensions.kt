package org.p2p.wallet.utils

import org.p2p.uikit.R
import org.p2p.uikit.atoms.UiKitTransactionImageView
import org.p2p.wallet.transaction.model.TransactionStatus

fun UiKitTransactionImageView.setStatus(status: TransactionStatus?) {
    setStatusIcon(status.getStatusIcon())
}

private fun TransactionStatus?.getStatusIcon(): Int? = when (this) {
    TransactionStatus.PENDING -> R.drawable.ic_state_pending
    TransactionStatus.ERROR -> R.drawable.ic_state_error
    else -> null
}
