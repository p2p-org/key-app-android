package org.p2p.wallet.bridge.send.statemachine

import java.math.BigDecimal

sealed class SendFeatureException(
    override val message: String? = null
) : Exception(message) {

    data class NotEnoughAmount(
        val invalidAmount: BigDecimal,
    ) : SendFeatureException()

    data class InsufficientFunds(
        val invalidAmount: BigDecimal,
    ) : SendFeatureException()

    data class FeeIsMoreThanAmount(
        val totalAmount: BigDecimal,
    ) : SendFeatureException()

    data class FeeLoadingError(
        override val message: String? = null
    ) : SendFeatureException(message)
}
