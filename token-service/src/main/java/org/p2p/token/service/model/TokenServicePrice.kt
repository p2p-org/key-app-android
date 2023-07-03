package org.p2p.token.service.model

import java.math.BigDecimal
import org.p2p.core.utils.scaleShort

class TokenServicePrice(
    val address: String,
    val rate: TokenRate?,
    val network: TokenServiceNetwork
) {
    fun getScaledUsdRate(): BigDecimal? = rate?.usd?.scaleShort()

    fun getUsdRate(): BigDecimal? = rate?.usd
}
