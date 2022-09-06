package org.p2p.wallet.moonpay.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

enum class Method {
    BANK_TRANSFER,
    CARD
}

data class PaymentMethod(
    val method: Method,
    var isSelected: Boolean,
    val feePercent: Float,
    @StringRes val paymentPeriodResId: Int,
    @StringRes val methodResId: Int,
    @DrawableRes val iconResId: Int,
    var paymentType: String,
)
