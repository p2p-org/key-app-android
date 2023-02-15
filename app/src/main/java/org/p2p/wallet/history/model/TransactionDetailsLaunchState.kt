package org.p2p.wallet.history.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.history.model.rpc.HistoryTransaction

sealed class TransactionDetailsLaunchState : Parcelable {
    @Parcelize
    data class Id(val tokenPublicKey: String, val transactionId: String) : TransactionDetailsLaunchState()

    @Parcelize
    data class History(val transaction: HistoryTransaction) : TransactionDetailsLaunchState()
}
