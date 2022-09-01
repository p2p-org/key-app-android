package org.p2p.wallet.moonpay.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.utils.asUsd
import org.p2p.wallet.utils.emptyString
import java.math.BigDecimal

@Parcelize
data class BuyViewData(
    val tokenSymbol: String,
    val currencySymbol: String,
    val price: BigDecimal,
    val receiveAmount: Double,
    val processingFee: BigDecimal,
    val networkFee: BigDecimal,
    val extraFee: BigDecimal,
    val accountCreationCost: BigDecimal?,
    val total: BigDecimal,
    val receiveAmountText: String,
    val purchaseCostText: String?,
) : Parcelable {

    val priceText: String
        get() = price.asUsd()

    val processingFeeText: String
        get() = processingFee.asUsd()

    val networkFeeText: String
        get() = networkFee.asUsd()

    val extraFeeText: String
        get() = extraFee.asUsd()

    val accountCreationCostText: String
        get() = accountCreationCost?.asUsd() ?: emptyString()

    val totalText: String
        get() = total.asUsd()
}
