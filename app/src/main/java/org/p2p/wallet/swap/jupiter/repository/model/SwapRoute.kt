package org.p2p.wallet.swap.jupiter.repository.model

import java.math.BigDecimal
import java.math.BigInteger

data class SwapRoute(
    val inAmountInLamports: BigInteger,
    val outAmountInLamports: BigInteger,
    val priceImpactPct: BigDecimal,
    val marketInfos: List<SwapMarketInformation>,
    val amountInLamports: BigInteger,
    @androidx.annotation.IntRange(from = 0, to = 10000)
    val slippageBps: Int,
    val otherAmountThreshold: String,
    val swapMode: JupiterSwapMode,
    val fees: SwapFees
)
