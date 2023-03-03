package org.p2p.wallet.swap.jupiter.statemanager

import java.math.BigDecimal
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.model.Slippage

sealed interface SwapStateAction {
    data class ActiveRouteChanged(val ordinalRouteNumber: Int) : SwapStateAction

    data class SlippageChanged(val newSlippageValue: Slippage) : SwapStateAction

    object InitialLoading : SwapStateAction

    object RefreshRoutes : SwapStateAction
    object EmptyAmountTokenA : SwapStateAction

    data class TokenAChanged(val newTokenA: SwapTokenModel) : SwapStateAction

    data class TokenBChanged(val newTokenB: SwapTokenModel) : SwapStateAction

    data class TokenAAmountChanged(
        val newAmount: BigDecimal
    ) : SwapStateAction

    object SwitchTokens : SwapStateAction
    object CancelSwapLoading : SwapStateAction
}
