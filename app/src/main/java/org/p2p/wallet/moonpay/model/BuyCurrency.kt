package org.p2p.wallet.moonpay.model

import org.p2p.wallet.moonpay.api.MoonpayBuyCurrencyResponse
import java.math.BigDecimal

data class BuyCurrency(
    val receiveAmount: Double,
    val price: BigDecimal,
    val feeAmount: BigDecimal,
    val extraFeeAmount: BigDecimal,
    val networkFeeAmount: BigDecimal,
    val totalAmount: BigDecimal,
    val baseCurrency: Currency,
    val quoteCurrency: Currency,
) {

    constructor(response: MoonpayBuyCurrencyResponse) : this(
        receiveAmount = response.quoteCurrencyAmount,
        price = response.quoteCurrencyPrice,
        feeAmount = response.feeAmount,
        extraFeeAmount = response.extraFeeAmount,
        networkFeeAmount = response.networkFeeAmount,
        totalAmount = response.totalAmount,
        baseCurrency = Currency(
            response.baseCurrency.code,
            response.baseCurrency.minBuyAmount,
            response.baseCurrency.maxBuyAmount
        ),
        quoteCurrency = Currency(
            response.currency.code,
            response.currency.minBuyAmount,
            response.currency.maxBuyAmount
        )
    )

    data class Currency(
        val code: String,
        val minAmount: BigDecimal,
        val maxAmount: BigDecimal?,
    )
}
