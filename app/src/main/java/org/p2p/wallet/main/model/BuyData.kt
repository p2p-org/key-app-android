package org.p2p.wallet.main.model

import org.p2p.wallet.utils.Constants
import java.math.BigDecimal

data class BuyData(
    val price: BigDecimal,
    val receiveAmount: BigDecimal,
    val processingFee: BigDecimal,
    val networkFee: BigDecimal,
    val extraFee: BigDecimal,
    val accountCreationCost: BigDecimal?,
    val total: BigDecimal
) {

    val receiveAmountText: String
        get() = "${Constants.USD_SYMBOL}$receiveAmount"

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