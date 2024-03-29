package org.p2p.wallet.moonpay.clientsideapi.response

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

class MoonpayTokenCurrencyResponse(
    @SerializedName("EUR")
    val amountInEur: BigDecimal,
    @SerializedName("GBP")
    val amountInGbp: BigDecimal,
    @SerializedName("USD")
    val amountInUsd: BigDecimal
)
