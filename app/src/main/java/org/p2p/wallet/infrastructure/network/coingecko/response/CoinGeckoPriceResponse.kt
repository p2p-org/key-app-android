package org.p2p.wallet.infrastructure.network.coingecko.response

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

class CoinGeckoPriceResponse(
    @SerializedName("id")
    val id: String,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("current_price")
    val currentPrice: BigDecimal
)
