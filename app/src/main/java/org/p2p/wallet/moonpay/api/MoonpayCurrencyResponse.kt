package org.p2p.wallet.moonpay.api

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class MoonpayCurrencyResponse(
    @SerializedName("EUR")
    val eur: BigDecimal,
    @SerializedName("GBP")
    val gbp: BigDecimal,
    @SerializedName("USD")
    val usd: BigDecimal
)
