package org.p2p.wallet.swap.jupiter.statemanager

import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import java.math.BigDecimal

sealed class SwapStateAction {
    data class ActiveRouteChanged(val ordinalRouteNumber: Int) : SwapStateAction()

    data class SlippageChanged(val newSlippageValue: Double) : SwapStateAction()

    object InitialLoading : SwapStateAction()

    object RefreshRoutes : SwapStateAction()
    object EmptyAmountTokenA : SwapStateAction()

    data class TokenAChanged(val newTokenA: SwapTokenModel) : SwapStateAction()

    data class TokenBChanged(val newTokenB: SwapTokenModel) : SwapStateAction()

    data class TokenAAmountChanged(
        val newAmount: BigDecimal
    ) : SwapStateAction()

    object SwitchTokens : SwapStateAction()
    object SwapSuccess : SwapStateAction()
}
