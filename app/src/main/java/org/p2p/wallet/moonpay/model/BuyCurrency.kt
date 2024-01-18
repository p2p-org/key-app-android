package org.p2p.wallet.moonpay.model

import android.os.Parcelable
import java.math.BigDecimal
import kotlinx.parcelize.Parcelize

@Parcelize
data class BuyCurrency(
    val receiveAmount: Double,
    val price: BigDecimal,
    val feeAmount: BigDecimal,
    val extraFeeAmount: BigDecimal,
    val networkFeeAmount: BigDecimal,
    val totalFiatAmount: BigDecimal,
    val baseCurrency: Currency,
    val quoteCurrency: Currency,
) : Parcelable {
    @Parcelize
    data class Currency(
        val code: String,
        val minAmount: BigDecimal,
        val maxAmount: BigDecimal?,
    ) : Parcelable {
        companion object {
            fun create(code: String): Currency = Currency(
                code = code,
                minAmount = BigDecimal.ZERO,
                maxAmount = null
            )
        }
    }
}
