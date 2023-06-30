package org.p2p.token.service.model

import java.math.BigDecimal
import org.p2p.core.utils.scaleShort

class TokenServicePrice(
    val address: String,
    val price: BigDecimal?
) {
    fun getScaledValue(): BigDecimal? = price?.scaleShort()

}
