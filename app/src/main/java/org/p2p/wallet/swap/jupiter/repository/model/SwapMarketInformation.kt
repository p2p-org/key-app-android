package org.p2p.wallet.swap.jupiter.repository.model

import org.p2p.wallet.utils.Base58String
import java.math.BigDecimal

data class SwapMarketInformation(
    val id: String,
    val label: String,
    val inputMint: Base58String,
    val outputMint: Base58String,
    val notEnoughLiquidity: Boolean,
    val inAmount: BigDecimal,
    val outAmount: BigDecimal,
    val minInAmount: BigDecimal?,
    val minOutAmount: BigDecimal?,
    val priceImpactPct: BigDecimal,
    val lpFee: LpFee,
    val platformFee: PlatformFeeRequest
) {
    data class LpFee(
        val amount: String,
        val mint: Base58String,
        val pct: BigDecimal
    )

    data class PlatformFeeRequest(
        val amount: String,
        val mint: Base58String,
        val pct: BigDecimal
    )
}
