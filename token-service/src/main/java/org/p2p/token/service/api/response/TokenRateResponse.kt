package org.p2p.token.service.api.response

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

internal class TokenRateResponse(
    @SerializedName("usd")
    val usd: BigDecimal?
)
