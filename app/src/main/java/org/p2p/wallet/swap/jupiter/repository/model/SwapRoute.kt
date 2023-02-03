package org.p2p.wallet.swap.jupiter.repository.model

import java.math.BigDecimal

data class SwapRoute(
    val inAmount: BigDecimal,
    val outAmount: BigDecimal,
    val priceImpactPct: BigDecimal,
    val marketInfos: List<SwapMarketInformation>,
    val amount: BigDecimal,
    @androidx.annotation.IntRange(from = 0, to = 10000)
    val slippageBps: Int,
    val otherAmountThreshold: String,
    val swapMode: JupiterSwapMode,
    val fees: SwapFees
)
