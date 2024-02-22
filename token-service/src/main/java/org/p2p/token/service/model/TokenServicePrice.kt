package org.p2p.token.service.model

import java.math.BigDecimal
import org.p2p.core.utils.scaleToTwo

data class TokenServicePrice(
    val tokenAddress: String,
    val rate: TokenRate,
    val network: TokenServiceNetwork
) {
    val scaledUsdRate: BigDecimal?
        get() = rate.usd?.scaleToTwo()

    val usdRate: BigDecimal?
        get() = rate.usd

    override fun toString(): String {
        return "TokenServicePrice($tokenAddress - ${rate.usd} - ${network.networkName})"
    }
}
