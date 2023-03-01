package org.p2p.wallet.swap.jupiter.statemanager

import java.math.BigDecimal

open class SwapFeatureException : Exception() {

    data class NotValidTokenA(
        val notValidAmount: BigDecimal,
    ) : SwapFeatureException()

    object SameTokens : SwapFeatureException()

    object RoutesNotFound : SwapFeatureException()
}
