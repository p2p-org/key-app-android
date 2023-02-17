package org.p2p.wallet.swap.jupiter.statemanager

import java.math.BigDecimal
import org.p2p.core.token.Token

sealed class SwapStateAction {
    data class ActiveRouteChanged(val ordinalRouteNumber: Int) : SwapStateAction()
    data class SlippageChanged(val newSlippageValue: Double) : SwapStateAction()

    data class InitialLoadingFinished(
        val tokenA: Token.Active,
        val tokenB: Token
    ) : SwapStateAction()

    object RefreshRoutes : SwapStateAction()

    data class TokenAChanged(val newTokenA: Token.Active) : SwapStateAction()
    data class TokenBChanged(val newTokenB: Token) : SwapStateAction()
    data class TokenAAmountChanged(
        val tokenA: Token.Active,
        val tokenB: Token,
        val slippage: Double,
        val newAmount: BigDecimal
    ) : SwapStateAction()
}
