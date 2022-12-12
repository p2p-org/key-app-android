package org.p2p.wallet.moonpay.clientsideapi.response

import com.google.gson.annotations.SerializedName

data class MoonpayCurrencyResponse(
    @SerializedName("id")
    val currencyId: String,
    @SerializedName("name")
    val currencyName: String,
    @SerializedName("code")
    val currencySymbol: String,
    @SerializedName("currencyType")
    val currencyType: String,
    @SerializedName("minAmount")
    val minAmount: Double,
    @SerializedName("maxAmount")
    val maxAmount: Double,
    @SerializedName("minSellAmount")
    val minSellAmount: Double,
    @SerializedName("maxSellAmount")
    val maxSellAmount: Double,
    @SerializedName("minBuyAmount")
    val minBuyAmount: Double,
    @SerializedName("maxBuyAmount")
    val maxBuyAmount: Double,
)
