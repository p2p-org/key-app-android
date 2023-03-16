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
        val remainingAmount: BigDecimal,
    ) : SwapFeatureException()

    object SameTokens : SwapFeatureException()

    object SmallTokenAAmount : SwapFeatureException()

    object RoutesNotFound : SwapFeatureException()
}
