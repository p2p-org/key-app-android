package org.p2p.market.price.api.response

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

internal class MarketPriceResponse(
    @SerializedName("usd")
    val usd: BigDecimal
)
