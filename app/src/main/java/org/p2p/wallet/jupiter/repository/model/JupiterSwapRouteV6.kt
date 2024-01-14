package org.p2p.wallet.jupiter.repository.model

import androidx.annotation.IntRange
import com.google.gson.JsonObject
import java.math.BigDecimal
import java.math.BigInteger
import org.p2p.core.crypto.Base58String

data class JupiterSwapRouteV6(
    val inAmountInLamports: BigInteger,
    val outAmountInLamports: BigInteger,
    val priceImpactPercent: BigDecimal,
    @IntRange(from = 0, to = 10000)
    val slippageBps: Int,
    val otherAmountThreshold: String,
    val swapMode: String,
    val ataFee: BigInteger,
    val routePlans: List<JupiterSwapRoutePlanV6>,
    // this field we need to create a route later
    val originalRoute: JsonObject
)

data class JupiterSwapRoutePlanV6(
    val ammKey: String,
    val feeAmount: BigInteger,
    val feeMint: Base58String,
    val label: String,
    val inputMint: Base58String,
    val outAmount: BigInteger,
    val outputMint: Base58String,
    val percent: String,
)
