package org.p2p.wallet.bridge.model

import java.math.BigDecimal
import org.p2p.core.utils.toPowerValue

data class BridgeBundleFee(
    val amount: BigDecimal,
    val amountInUsd: BigDecimal
) {
    fun amountInToken(decimals: Int): BigDecimal = amount.divide(decimals.toPowerValue())
}
