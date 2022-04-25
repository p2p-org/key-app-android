package org.p2p.wallet.moonpay.api

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

class MoonpayCurrencyResponse(
    @SerializedName("EUR")
    val amountInEur: BigDecimal,
    @SerializedName("GBP")
    val amountInGbp: BigDecimal,
    @SerializedName("USD")
    val amountInUsd: BigDecimal
)
