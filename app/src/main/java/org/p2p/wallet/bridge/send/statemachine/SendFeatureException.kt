package org.p2p.wallet.bridge.send.statemachine

import java.math.BigDecimal

sealed class SendFeatureException(
    override val message: String? = null,
    open val amount: BigDecimal,
) : Exception(message) {

    data class NotEnoughAmount(
        override val amount: BigDecimal,
    ) : SendFeatureException(amount = amount)

    data class InsufficientFunds(
        override val amount: BigDecimal,
    ) : SendFeatureException(amount = amount)

    data class FeeIsMoreThanAmount(
        override val amount: BigDecimal,
    ) : SendFeatureException(amount = amount)

    data class FeeLoadingError(
        override val message: String? = null,
        override val amount: BigDecimal,
    ) : SendFeatureException(message, amount)
}
