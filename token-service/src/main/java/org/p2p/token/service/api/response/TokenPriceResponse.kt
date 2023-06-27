package org.p2p.token.service.api.response

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

internal class TokenPriceResponse(
    @SerializedName("usd")
    val usd: BigDecimal
)
