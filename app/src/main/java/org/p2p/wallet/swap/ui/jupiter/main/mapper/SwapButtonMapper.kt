package org.p2p.wallet.swap.ui.jupiter.main.mapper

import org.p2p.core.common.TextContainer
import org.p2p.wallet.R
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.ui.jupiter.main.SwapButtonState

class SwapButtonMapper {

    fun mapLoading(): SwapButtonState.Disabled =
        SwapButtonState.Disabled(TextContainer(R.string.swap_main_button_loading))

    fun mapEnterAmount(): SwapButtonState.Disabled =
        SwapButtonState.Disabled(TextContainer(R.string.swap_main_button_enter_amount))

    fun mapRouteNotFound(): SwapButtonState.Disabled =
        SwapButtonState.Disabled(TextContainer(R.string.swap_main_button_route_not_found))

    fun mapSameToken(): SwapButtonState.Disabled =
        SwapButtonState.Disabled(TextContainer(R.string.swap_main_button_route_not_found))

    fun mapTokenAmountNotEnough(tokenA: SwapTokenModel?): SwapButtonState.Disabled =
        tokenA?.tokenSymbol
            ?.let { symbol ->
                SwapButtonState.Disabled(TextContainer(R.string.swap_main_button_not_enough_amount, symbol))
            } ?: mapEnterAmount()

    fun mapReadyToSwap(tokenA: SwapTokenModel, tokenB: SwapTokenModel): SwapButtonState.ReadyToSwap {
        val firstName = tokenA.tokenSymbol
        val secondName = tokenB.tokenSymbol
        return SwapButtonState.ReadyToSwap(
            text = TextContainer(R.string.swap_main_button_ready_to_swap, firstName, secondName)
        )
    }
}
