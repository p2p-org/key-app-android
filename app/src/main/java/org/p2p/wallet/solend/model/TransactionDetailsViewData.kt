package org.p2p.wallet.solend.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TransactionDetailsViewData(
    val amount: String,
    val transferFee: String?, // null == free
    val fee: String,
    val total: String
) : Parcelable
