package org.p2p.ethereumkit.external.api.coingecko.response

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

internal data class CoinGeckoPriceResponse(
    @SerializedName("usd")
    val priceInUsd: BigDecimal
)
