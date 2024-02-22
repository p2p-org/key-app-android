package org.p2p.wallet.jupiter.repository.model

import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.crypto.Base58String
import org.p2p.core.utils.scaleToTwoOrFirstNotZero

@Deprecated("Old v4 swap logic")
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
    val liquidityFee: LpFee,
    val platformFee: PlatformFee
) {
    @Deprecated("Old v4 swap logic")
    data class LpFee(
        val amountInLamports: BigInteger,
        val mint: Base58String,
        val percent: BigDecimal
    ) {
        val formattedPercent: BigDecimal = percent.times(BigDecimal(100)).scaleToTwoOrFirstNotZero()
    }

    @Deprecated("Old v4 swap logic")
    data class PlatformFee(
        val amountInLamports: BigInteger,
        val mint: Base58String,
        val percent: BigDecimal
    ) {
        val formattedPercent: BigDecimal = percent.times(BigDecimal(100)).scaleToTwoOrFirstNotZero()
    }
}
