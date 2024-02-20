package org.p2p.token.service.api.coingecko.response

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

internal class CoinGeckoEthereumPriceResponse(
    @SerializedName("usd")
    val priceInUsd: BigDecimal?
)
