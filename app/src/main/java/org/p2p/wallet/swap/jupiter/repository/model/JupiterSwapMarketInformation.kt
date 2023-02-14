package org.p2p.wallet.swap.jupiter.repository.model

import org.p2p.wallet.utils.Base58String
import java.math.BigDecimal
import java.math.BigInteger

data class JupiterSwapMarketInformation(
    val id: String,
    val label: String,
    val inputMint: Base58String,
    val outputMint: Base58String,
    val notEnoughLiquidity: Boolean,
    val inAmountInLamports: BigInteger,
    val outAmountInLamports: BigInteger,
    val minInAmountInLamports: BigInteger?,
    val minOutAmountInLamports: BigInteger?,
    val priceImpactPct: BigDecimal,
    val lpFee: LpFee,
    val platformFee: PlatformFee
) {
    data class LpFee(
        val amountInLamports: BigInteger,
        val mint: Base58String,
        val pct: BigDecimal
    )

    data class PlatformFee(
        val amountInLamports: BigInteger,
        val mint: Base58String,
        val pct: BigDecimal
    )
}
