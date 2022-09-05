package org.p2p.wallet.moonpay.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.p2p.wallet.utils.Constants
import org.p2p.wallet.utils.Constants.USD_SYMBOL
import org.p2p.wallet.utils.asCurrency
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
    val receiveAmountText: String?,
    val purchaseCostText: String?,
) : Parcelable {

    val priceText: String
        get() = price.asCurrency(currency)

    val processingFeeText: String
        get() = processingFee.asCurrency(currency)

    val networkFeeText: String
        get() = networkFee.asCurrency(currency)

    val extraFeeText: String
        get() = extraFee.asCurrency(currency)

    val accountCreationCostText: String
        get() = accountCreationCost?.asCurrency(currency) ?: emptyString()

    val totalText: String
        get() = total.asCurrency(currency)

    val currency: String
        get() = if (currencySymbol == Constants.USD_READABLE_SYMBOL) USD_SYMBOL else currencySymbol
}
