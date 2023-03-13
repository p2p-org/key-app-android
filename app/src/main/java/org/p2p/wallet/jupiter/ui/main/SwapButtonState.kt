package org.p2p.wallet.jupiter.ui.main

import org.p2p.core.common.TextContainer

sealed interface SwapButtonState {

    object Hide : SwapButtonState

    data class Disabled(
        val text: TextContainer
    ) : SwapButtonState

    data class ReadyToSwap(
        val text: TextContainer
    ) : SwapButtonState
}
