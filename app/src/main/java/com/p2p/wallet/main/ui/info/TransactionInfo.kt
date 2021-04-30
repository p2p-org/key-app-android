package com.p2p.wallet.main.ui.info

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import java.math.BigDecimal

data class TransactionInfo(
    val transactionId: String,
    @StringRes val status: Int,
    @StringRes val message: Int,
    @DrawableRes val iconRes: Int,
    val amount: BigDecimal,
    val usdAmount: BigDecimal,
    val tokenSymbol: String
)