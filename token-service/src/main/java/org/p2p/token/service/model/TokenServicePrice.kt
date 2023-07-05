package org.p2p.token.service.model

import java.math.BigDecimal
import org.p2p.core.utils.scaleShort

class TokenServicePrice(
    val address: String,
    val rate: TokenRate,
    val network: TokenServiceNetwork
) {
    val scaledUsdRate: BigDecimal?
        get() = rate.usd?.scaleShort()

    val usdRate: BigDecimal?
        get() = rate.usd
}
