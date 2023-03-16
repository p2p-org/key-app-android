package org.p2p.wallet.bridge.model

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import org.p2p.core.utils.orZero
import org.p2p.core.utils.toPowerValue

data class BridgeBundleFee(
    @SerializedName("amount")
    val amount: String?,
    @SerializedName("usd_amount")
    val amountInUsd: String?,
    @SerializedName("chain")
    val chain: String?,
    @SerializedName("token")
    val token: String?,
) {
    fun amountInToken(decimals: Int): BigDecimal =
        amount?.toBigDecimal()?.orZero()?.divide(decimals.toPowerValue()) ?: BigDecimal.ZERO
}
