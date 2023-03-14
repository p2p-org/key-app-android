package org.p2p.wallet.claim.api.response

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.math.BigInteger

class FeeResponse(
    val amount: BigInteger,
    @SerializedName("usd_amount")
    val usdAmount: BigDecimal
)
