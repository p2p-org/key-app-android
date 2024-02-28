package org.p2p.wallet.jupiter.api.response

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class SwapJupiterV6QuoteResponse(
    @SerializedName("inputMint")
    val inputMint: String,
    @SerializedName("inAmount")
    val inAmount: String,
    @SerializedName("outputMint")
    val outputMint: String,
    @SerializedName("outAmount")
    val outAmount: String,
    @SerializedName("otherAmountThreshold")
    val otherAmountThreshold: String,
    @SerializedName("swapMode")
    val swapMode: String,
    @SerializedName("slippageBps")
    val slippageBps: Int,
    @SerializedName("platformFee")
    val platformFee: PlatformFeeResponse?,
    @SerializedName("priceImpactPct")
    val priceImpactPct: String,
    @SerializedName("routePlan")
    val routePlan: List<RoutePlanResponse>,
    @SerializedName("keyapp")
    val keyAppFees: KeyAppFeesResponse,
    @SerializedName("contextSlot")
    val contextSlot: Int,
    @SerializedName("timeTaken")
    val timeTaken: Double
) {
    data class PlatformFeeResponse(
        @SerializedName("amount")
        val amount: String,
        @SerializedName("feeBps")
        val feeBps: BigDecimal
    )

    data class RoutePlanResponse(
        @SerializedName("swapInfo")
        val swapInfo: RoutePlanDetailsResponse,
        @SerializedName("percent")
        val percent: Int
    )

    data class RoutePlanDetailsResponse(
        @SerializedName("ammKey")
        val ammKey: String,
        @SerializedName("label")
        val label: String,
        @SerializedName("inputMint")
        val inputMint: String,
        @SerializedName("outputMint")
        val outputMint: String,
        @SerializedName("inAmount")
        val inAmount: String,
        @SerializedName("outAmount")
        val outAmount: String,
        @SerializedName("feeAmount")
        val feeAmount: String,
        @SerializedName("feeMint")
        val feeMint: String
    )

    data class KeyAppFeesResponse(
        @SerializedName("fee")
        val fee: String,
        @SerializedName("refundableFee")
        val refundableFee: String,
        @SerializedName("_hash")
        val hash: String,
        @SerializedName("epoch")
        val epoch: Long,
        @SerializedName("fees")
        val feeDetails: JsonObject
    )
}
