package org.p2p.wallet.moonpay.model

import org.p2p.wallet.utils.Constants
import java.math.BigDecimal

data class BuyData(
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
) {

    val priceText: String
        get() = "${Constants.USD_SYMBOL}$price"

    val processingFeeText: String
        get() = "${Constants.USD_SYMBOL}$processingFee"

    val networkFeeText: String
        get() = "${Constants.USD_SYMBOL}$networkFee"

    val extraFeeText: String
        get() = "${Constants.USD_SYMBOL}$extraFee"

    val accountCreationCostText: String
        get() = "${Constants.USD_SYMBOL}$accountCreationCost"

    val totalText: String
        get() = "${Constants.USD_SYMBOL}$total"
}
