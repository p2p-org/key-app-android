package org.p2p.wallet.swap.model

import org.p2p.wallet.swap.model.orca.SwapTotal

sealed interface SwapDetailsState {
    data class Shown(val fee: SwapTotal) : SwapDetailsState
}
