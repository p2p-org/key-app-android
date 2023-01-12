package org.p2p.wallet.moonpay.clientsideapi.response

import com.google.gson.annotations.SerializedName

data class MoonpaySellQuoteResponse(
    @SerializedName("baseCurrency")
    val tokenDetails: MoonpayCurrencyResponse,
    @SerializedName("baseCurrencyAmount")
    val tokenAmount: Double,
    @SerializedName("baseCurrencyPrice")
    val tokenPrice: Double,
    @SerializedName("quoteCurrency")
    val fiatDetails: MoonpayCurrencyResponse,
    @SerializedName("paymentMethod")
    val paymentMethod: String,
    @SerializedName("extraFeeAmount")
    val extraFeeAmount: Int,
    @SerializedName("feeAmount")
    val feeAmount: Double,
    @SerializedName("quoteCurrencyAmount")
    val fiatEarning: Double
)
