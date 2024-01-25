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
    val routePlans: List<JupiterSwapRoutePlanV6>,
    val fees: SwapKeyAppFees,
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

data class SwapKeyAppFees(
    // maybe totalFees, maybe else,
    // I don't know what fees, no doc at the moment
    val totalFees: BigInteger,
    val signatureFee: BigInteger,
    val ataDeposits: BigInteger,
    val totalFeeAndDeposits: BigInteger,
    val minimumSolForTransaction: BigInteger,
)
