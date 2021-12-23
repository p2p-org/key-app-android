package org.p2p.wallet.main.api

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class MoonpayBuyCurrencyResponse(
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
    val totalAmount: BigDecimal
)