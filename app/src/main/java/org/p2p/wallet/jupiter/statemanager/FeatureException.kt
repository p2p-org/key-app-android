package org.p2p.wallet.jupiter.statemanager

import java.math.BigDecimal

open class FeatureException : Exception() {

    data class NotValidTokenA(
        val notValidAmount: BigDecimal,
    ) : FeatureException()

    object RoutesNotFound : FeatureException()
}
