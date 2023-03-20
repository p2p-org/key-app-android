package org.p2p.wallet.bridge.api.response

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

class BridgeBundleFeeResponse(
    @SerializedName("amount")
    val amount: BigDecimal,
    @SerializedName("usd_amount")
    val usdAmount: BigDecimal
)
