package org.p2p.wallet.history.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class TransactionDetailsLaunchState : Parcelable {
    @Parcelize
    data class Id(val transactionId: String) : TransactionDetailsLaunchState()

    @Parcelize
    data class History(val transaction: HistoryTransaction) : TransactionDetailsLaunchState()
}
