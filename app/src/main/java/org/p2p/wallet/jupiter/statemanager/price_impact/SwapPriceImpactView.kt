package org.p2p.wallet.jupiter.statemanager.price_impact

sealed interface SwapPriceImpactView {
    object Hidden : SwapPriceImpactView
    data class Yellow(val warningText: String) : SwapPriceImpactView
    data class Red(val warningText: String) : SwapPriceImpactView
}
