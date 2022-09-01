package org.p2p.wallet.moonpay.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class BuyCurrency(
    val receiveAmount: Double,
    val price: BigDecimal,
    val feeAmount: BigDecimal,
    val extraFeeAmount: BigDecimal,
    val networkFeeAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val baseCurrency: Currency,
    val quoteCurrency: Currency,
) : Parcelable {
    @Parcelize
    data class Currency(
        val code: String,
        val minAmount: BigDecimal,
        val maxAmount: BigDecimal?,
    ) : Parcelable
}
