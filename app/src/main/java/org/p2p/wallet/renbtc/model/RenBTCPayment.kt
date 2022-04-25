package org.p2p.wallet.renbtc.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RenBTCPayment(
    val transactionHash: String,
    val txIndex: Int,
    val amount: Long
) : Parcelable
