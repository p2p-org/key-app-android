package org.p2p.wallet.swap.jupiter.statemanager.price_impact

import java.math.BigDecimal
import org.p2p.core.utils.isLessThan

enum class SwapPriceImpact {

    NORMAL, YELLOW, RED
}

private val threePercent
    get() = BigDecimal.valueOf(0.3)

private val onePercent
    get() = BigDecimal.valueOf(0.1)

fun BigDecimal.toPriceImpactType(): SwapPriceImpact {
    return when {
        isLessThan(onePercent) -> SwapPriceImpact.NORMAL
        isLessThan(threePercent) -> SwapPriceImpact.YELLOW
        else -> SwapPriceImpact.RED
    }
}
