package org.p2p.wallet.main.model

import org.p2p.wallet.utils.Constants
import java.math.BigDecimal

data class BuyData(
    val price: BigDecimal,
    val processingFee: BigDecimal,
    val networkFee: BigDecimal,
    val accountCreationCost: BigDecimal
) {

    val priceText: String
        get() = "${Constants.USD_SYMBOL}$price"

    val processingFeeText: String
        get() = "${Constants.USD_SYMBOL}$processingFee"

    val networkFeeText: String
        get() = "${Constants.USD_SYMBOL}$networkFee"

    val accountCreationCostText: String
        get() = "${Constants.USD_SYMBOL}$accountCreationCost"

    val total: String
        get() = "${Constants.USD_SYMBOL}${processingFee + networkFee + accountCreationCost}"
}