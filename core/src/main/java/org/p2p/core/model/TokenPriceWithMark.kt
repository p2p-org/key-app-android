package org.p2p.core.model

import java.math.BigDecimal
import java.time.ZonedDateTime

const val DEFAULT_EXPIRATION_TIME_SEC = 60

class TokenPriceWithMark(
    val priceInUsd: BigDecimal,
    val timestamp: Long = ZonedDateTime.now().toEpochSecond()
) {
    fun isValid(): Boolean {
        return ZonedDateTime.now().toEpochSecond() - timestamp < DEFAULT_EXPIRATION_TIME_SEC
    }
}
