package org.p2p.wallet.send.ui.dialogs

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class TransactionInfo(
    val transactionId: String,
    @StringRes val status: Int,
    @StringRes val message: Int,
    @DrawableRes val iconRes: Int,
    val amount: String,
    val usdAmount: String,
    val tokenSymbol: String
) {

    fun getFormattedAmount(): String = "$amount $tokenSymbol"
}
