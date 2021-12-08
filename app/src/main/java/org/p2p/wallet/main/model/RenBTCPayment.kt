package org.p2p.wallet.main.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.renbtc.ui.main.BTC_DECIMALS
import org.p2p.wallet.utils.fromLamports
import org.p2p.wallet.utils.scaleMedium

@Parcelize
data class RenBTCPayment(
    val transactionHash: String,
    val txIndex: Int,
    val amount: Long
) : Parcelable {

    fun getAmount(): String = amount.toBigInteger().fromLamports(BTC_DECIMALS).scaleMedium().toString()
}