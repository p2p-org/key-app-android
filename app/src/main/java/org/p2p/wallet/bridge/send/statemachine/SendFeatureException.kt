package org.p2p.wallet.bridge.send.statemachine

import java.math.BigDecimal

open class SendFeatureException(
    override val message: String? = null
) : Exception(message) {

    data class NotEnoughAmount(
        val amount: BigDecimal,
    ) : SendFeatureException()

    data class FeeLoadingError(
        override val message: String? = null
    ) : SendFeatureException(message)
}
