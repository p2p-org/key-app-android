package org.p2p.token.service.api.coingecko.response

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

internal class CoinGeckoSolPriceResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("current_price")
    val currentPrice: BigDecimal
)
