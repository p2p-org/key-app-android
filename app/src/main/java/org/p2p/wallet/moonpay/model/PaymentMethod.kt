package org.p2p.wallet.moonpay.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class PaymentMethod(
    val method: MethodType,
    var isSelected: Boolean,
    val feePercent: Float,
    @StringRes val paymentPeriodResId: Int,
    @StringRes val methodResId: Int,
    @DrawableRes val iconResId: Int,
    var paymentType: String,
) {
    enum class MethodType {
        BANK_TRANSFER,
        CARD
    }
}
