package org.p2p.wallet.jupiter.interactor.model

import java.math.BigDecimal
import org.p2p.wallet.swap.model.Slippage

sealed interface SwapPriceImpactType {
    object None : SwapPriceImpactType

    enum class HighPriceImpactType {
        YELLOW, RED
    }

    data class HighPriceImpact(val priceImpactValue: BigDecimal, val type: HighPriceImpactType) : SwapPriceImpactType
    data class HighFees(val currentSlippage: Slippage) : SwapPriceImpactType
}
