package org.p2p.wallet.home.api

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

class CoinGeckoEthereumPrice(
    @SerializedName("usd")
    val currentPrice: BigDecimal
)
