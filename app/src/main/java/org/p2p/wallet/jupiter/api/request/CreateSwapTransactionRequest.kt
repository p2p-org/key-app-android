package org.p2p.wallet.jupiter.api.request

import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base58String
import org.p2p.wallet.jupiter.repository.model.JupiterSwapMode

@Deprecated("Old v4 swap logic")
data class CreateSwapTransactionRequest(
    @SerializedName("route")
    val route: SwapRouteRequest,
    @SerializedName("userPublicKey")
    val userPublicKey: Base58String
)

@Deprecated("Old v4 swap logic")
data class SwapRouteRequest(
    @SerializedName("inAmount")
    val inAmount: String,
    @SerializedName("outAmount")
    val outAmount: String,
    @SerializedName("priceImpactPct")
    val priceImpactPct: Double,
    @SerializedName("marketInfos")
    val marketInfos: List<MarketInfoRequest>,
    @SerializedName("amount")
    val amount: String,
    @SerializedName("slippageBps")
    @androidx.annotation.IntRange(from = 0, to = 10000)
    val slippageBps: Int,
    /*
     * The threshold for the swap based on the provided slippage:
     * when swapMode is ExactIn the minimum out amount,
     * when swapMode is ExactOut the maximum in amount
     */
    @SerializedName("otherAmountThreshold")
    val otherAmountThreshold: String,
    @SerializedName("swapMode")
    val swapMode: JupiterSwapMode,
    @SerializedName("fees")
    val fees: JupiterSwapFeesRequest,
    @SerializedName("keyapp")
    val keyAppFees: KeyAppFees
) {

    @Deprecated("Old v4 swap logic")
    data class MarketInfoRequest(
        @SerializedName("id")
        val id: String,
        @SerializedName("label")
        val label: String,
        @SerializedName("inputMint")
        val inputMint: String,
        @SerializedName("outputMint")
        val outputMint: String,
        @SerializedName("notEnoughLiquidity")
        val notEnoughLiquidity: Boolean,
        @SerializedName("inAmount")
        val inAmount: String,
        @SerializedName("outAmount")
        val outAmount: String,
        @SerializedName("minInAmount")
        val minInAmount: String?,
        @SerializedName("minOutAmount")
        val minOutAmount: String?,
        @SerializedName("priceImpactPct")
        val priceImpactPct: Double,
        @SerializedName("lpFee")
        val lpFee: LpFeeRequest,
        @SerializedName("platformFee")
        val platformFee: PlatformFeeRequest
    ) {
        @Deprecated("Old v4 swap logic")
        data class LpFeeRequest(
            @SerializedName("amount")
            val amount: String,
            @SerializedName("mint")
            val mint: String,
            @SerializedName("pct")
            val pct: Double
        )
        @Deprecated("Old v4 swap logic")
        data class PlatformFeeRequest(
            @SerializedName("amount")
            val amountInLamports: String,
            @SerializedName("mint")
            val mint: String,
            @SerializedName("pct")
            val pct: Double
        )
    }
    @Deprecated("Old v4 swap logic")
    data class KeyAppFees(
        @SerializedName("fee")
        val fee: String,
        @SerializedName("refundableFee")
        val refundableFee: String,
        @SerializedName("_hash")
        val hash: String
    )
}
@Deprecated("Old v4 swap logic")
data class JupiterSwapFeesRequest(
    @SerializedName("signatureFee")
    val signatureFeeInLamports: Long,
    @SerializedName("openOrdersDeposits")
    /**
     * the total amount needed for deposit of serum order account(s).
     */
    val openOrdersDepositsLamports: List<Long>,
    /**
     * the total amount needed for deposit of associative token account(s).
     */
    @SerializedName("ataDeposits")
    val ataDeposits: List<Long>,
    /**
     * the total lamports needed for fees and deposits above
     */
    @SerializedName("totalFeeAndDeposits")
    val totalFeeAndDepositsLamports: Long,
    /**
     * the minimum lamports needed for transaction(s)
     */
    @SerializedName("minimumSOLForTransaction")
    val minimumSolForTransactionLamports: Long,
)
