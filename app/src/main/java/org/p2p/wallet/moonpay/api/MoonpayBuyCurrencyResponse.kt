package org.p2p.wallet.moonpay.api

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

class MoonpayBuyCurrencyResponse(
    @SerializedName("quoteCurrencyPrice")
    val quoteCurrencyPrice: BigDecimal,
    @SerializedName("feeAmount")
    val feeAmount: BigDecimal,
    @SerializedName("extraFeeAmount")
    val extraFeeAmount: BigDecimal,
    @SerializedName("networkFeeAmount")
    val networkFeeAmount: BigDecimal,
    @SerializedName("quoteCurrencyAmount")
    val quoteCurrencyAmount: Double,
    @SerializedName("totalAmount")
    val totalAmount: BigDecimal,
    @SerializedName("baseCurrency")
    val baseCurrency: BaseCurrencyResponse,
    @SerializedName("currency")
    val currency: BaseCurrencyResponse
) {
    class BaseCurrencyResponse(
        @SerializedName("name")
        val name: String,
        @SerializedName("code")
        val code: String,
        @SerializedName("minBuyAmount")
        val minBuyAmount: BigDecimal,
        @SerializedName("maxBuyAmount")
        val maxBuyAmount: BigDecimal?,
    )
}
