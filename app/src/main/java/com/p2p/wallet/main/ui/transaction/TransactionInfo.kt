package com.p2p.wallet.main.ui.transaction

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
)