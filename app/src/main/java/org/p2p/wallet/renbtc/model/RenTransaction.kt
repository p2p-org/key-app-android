package org.p2p.wallet.renbtc.model

import android.content.Context
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.R
import org.p2p.wallet.main.model.RenBTCPayment
import org.p2p.wallet.utils.scaleMedium

@Parcelize
data class RenTransaction(
    val transactionId: String,
    val payment: RenBTCPayment,
    val status: RenTransactionStatus
) : Parcelable {

    fun getTransactionTitle(context: Context): String =
        if (status is RenTransactionStatus.SuccessfullyMinted) {
            context.getString(R.string.receive_renbtc_format, status.amount.scaleMedium())
        } else {
            context.getString(R.string.main_mint_renbtc)
        }
}