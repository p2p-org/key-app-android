package org.p2p.wallet.solend.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface SolendTransactionDetailsState : Parcelable {
    @Parcelize
    data class Deposit(val deposit: TransactionDetailsViewData) : SolendTransactionDetailsState

    @Parcelize
    data class Withdraw(val withdraw: TransactionDetailsViewData) : SolendTransactionDetailsState
}
