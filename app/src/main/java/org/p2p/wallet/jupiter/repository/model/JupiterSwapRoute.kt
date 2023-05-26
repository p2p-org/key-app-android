package org.p2p.wallet.jupiter.repository.model

import androidx.annotation.IntRange
import java.math.BigDecimal
import java.math.BigInteger

/**
 * @param keyAppFeeInLamports fee in lamports of out token
 */
data class JupiterSwapRoute(
    val inAmountInLamports: BigInteger,
    val outAmountInLamports: BigInteger,
    val priceImpactPct: BigDecimal,
    val marketInfos: List<JupiterSwapMarketInformation>,
    val amountInLamports: BigInteger,
    @IntRange(from = 0, to = 10000)
    val slippageBps: Int,
    val otherAmountThreshold: String,
    val swapMode: JupiterSwapMode,
    val fees: JupiterSwapFees,
    val keyAppFeeInLamports: BigInteger,
    val keyAppRefundableFee: String,
    val keyAppHash: String
)
