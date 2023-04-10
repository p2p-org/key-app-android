package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class BridgeFreeFeeLimitResponse(
    @SerializedName("result")
    val minUsdForFreeFee: BigDecimal
)
