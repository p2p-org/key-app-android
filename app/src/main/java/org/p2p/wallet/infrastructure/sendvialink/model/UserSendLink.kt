package org.p2p.wallet.infrastructure.sendvialink.model

import java.math.BigDecimal
import org.p2p.core.token.Token

data class UserSendLink(
    val link: String,
    val token: Token,
    val amount: BigDecimal,
    val dateCreated: Long
) {
    val amountInUsd: BigDecimal
        get() = token.usdRateOrZero.multiply(amount)
}
