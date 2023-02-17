package org.p2p.wallet.swap.jupiter.statemanager

import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import java.math.BigDecimal

sealed class SwapStateAction {
    data class ActiveRouteChanged(val ordinalRouteNumber: Int) : SwapStateAction()
    data class SlippageChanged(val newSlippageValue: Double) : SwapStateAction()

    data class InitialLoadingFinished(
        val tokenA: SwapTokenModel,
        val tokenB: SwapTokenModel
    ) : SwapStateAction()

    object RefreshRoutes : SwapStateAction()

    data class TokenAChanged(val newTokenA: SwapTokenModel) : SwapStateAction()
    data class TokenBChanged(val newTokenB: SwapTokenModel) : SwapStateAction()
    data class TokenAAmountChanged(
        val tokenA: SwapTokenModel,
        val tokenB: SwapTokenModel,
        val slippage: Double,
        val newAmount: BigDecimal
    ) : SwapStateAction()
}
