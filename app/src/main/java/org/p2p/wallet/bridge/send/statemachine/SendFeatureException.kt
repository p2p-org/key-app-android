package org.p2p.wallet.bridge.send.statemachine

import java.math.BigDecimal

open class SendFeatureException : Exception() {

    data class NotEnoughAmount(
        val amount: BigDecimal,
    ) : SendFeatureException()

    object FeeLoadingError : SendFeatureException()
}
