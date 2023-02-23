package org.p2p.wallet.swap.ui.jupiter.main.mapper

import org.p2p.core.common.TextContainer
import org.p2p.wallet.R
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.swap.ui.jupiter.main.SwapButtonState

class SwapButtonMapper {

    fun mapLoading(): SwapButtonState.Disabled =
        SwapButtonState.Disabled(TextContainer(R.string.swap_main_button_loading))

    fun mapEnterAmount(): SwapButtonState.Disabled =
        SwapButtonState.Disabled(TextContainer(R.string.swap_main_button_enter_amount))

    fun mapReadyToSwap(tokenA: SwapTokenModel, tokenB: SwapTokenModel): SwapButtonState.ReadyToSwap {
        val firstName = tokenA.tokenSymbol
        val secondName = tokenB.tokenSymbol
        return SwapButtonState.ReadyToSwap(
            text = TextContainer(R.string.swap_main_button_ready_to_swap, firstName, secondName)
        )
    }
}
