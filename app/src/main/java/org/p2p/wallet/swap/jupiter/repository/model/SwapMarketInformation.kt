package org.p2p.wallet.swap.jupiter.repository.model

import org.p2p.wallet.utils.Base58String
import java.math.BigDecimal
import java.math.BigInteger

data class SwapMarketInformation(
    val id: String,
    val label: String,
    val inputMint: Base58String,
    val outputMint: Base58String,
    val notEnoughLiquidity: Boolean,
    val inAmount: BigInteger,
    val outAmount: BigInteger,
    val minInAmount: BigInteger?,
    val minOutAmount: BigInteger?,
    val priceImpactPct: BigDecimal,
    val lpFee: LpFee,
    val platformFee: PlatformFeeRequest
) {
    data class LpFee(
        val amountInLamports: BigInteger,
        val mint: Base58String,
        val pct: BigDecimal
    )

    data class PlatformFeeRequest(
        val amountInLamports: BigInteger,
        val mint: Base58String,
        val pct: BigDecimal
    )
}
