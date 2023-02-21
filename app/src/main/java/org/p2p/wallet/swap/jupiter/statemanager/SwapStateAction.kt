package org.p2p.wallet.swap.jupiter.statemanager

import java.math.BigDecimal
import org.p2p.core.token.Token
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel

sealed class SwapStateAction {
    data class ActiveRouteChanged(val ordinalRouteNumber: Int) : SwapStateAction()

    data class SlippageChanged(val newSlippageValue: Double) : SwapStateAction()

    data class InitialLoadingFinished(
        val tokenA: Token.Active,
        val tokenB: Token
    ) : SwapStateAction()

    object RefreshRoutes : SwapStateAction()

    data class TokenAChanged(val newTokenA: SwapTokenModel) : SwapStateAction()

    data class TokenBChanged(val newTokenB: SwapTokenModel) : SwapStateAction()

    data class TokenAAmountChanged(
        val newAmount: BigDecimal
    ) : SwapStateAction()

    object SwitchTokens : SwapStateAction()
    object SwapSuccess : SwapStateAction()
}
