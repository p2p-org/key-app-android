package org.p2p.wallet.jupiter.statemanager

import java.math.BigDecimal

open class SwapFeatureException : Exception() {

    data class NotValidTokenA(
        val notValidAmount: BigDecimal,
    ) : SwapFeatureException()

    data class InsufficientSolBalance(val inputAmount: BigDecimal) : SwapFeatureException()

    object SameTokens : SwapFeatureException()

    object RoutesNotFound : SwapFeatureException()
}
