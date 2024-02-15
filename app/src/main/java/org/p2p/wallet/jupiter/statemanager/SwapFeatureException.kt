package org.p2p.wallet.jupiter.statemanager

import java.math.BigDecimal
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel

open class SwapFeatureException : Exception() {

    data class NotValidTokenA(
        val notValidAmount: BigDecimal,
    ) : SwapFeatureException()

    data class InsufficientSolBalance(
        val inputAmount: BigDecimal,
        val userSolToken: SwapTokenModel.UserToken,
        val allowedAmount: BigDecimal,
    ) : SwapFeatureException()

    object SameTokens : SwapFeatureException()

    data class SmallTokenAAmount(
        val notValidAmount: BigDecimal
    ) : SwapFeatureException()

    object RoutesNotFound : SwapFeatureException()
}
