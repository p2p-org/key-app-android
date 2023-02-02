package org.p2p.wallet.swap.jupiter.api.response.quote

import com.google.gson.annotations.SerializedName
import org.p2p.wallet.swap.jupiter.api.request.JupiterSwapFeesRequest
import org.p2p.wallet.swap.jupiter.api.request.SwapRouteRequest
import org.p2p.wallet.swap.jupiter.repository.model.JupiterSwapMode

data class JupiterRouteResponse(
    @SerializedName("amount")
    val amount: String,
    @SerializedName("inAmount")
    val inAmount: String,
    @SerializedName("marketInfos")
    val marketInfos: List<SwapRouteRequest.MarketInfoRequest>,
    @SerializedName("otherAmountThreshold")
    val otherAmountThreshold: String,
    @SerializedName("outAmount")
    val outAmount: String,
    @SerializedName("priceImpactPct")
    val priceImpactPct: Double,
    @SerializedName("slippageBps")
    val slippageBps: Int,
    @SerializedName("swapMode")
    val swapMode: JupiterSwapMode,
    @SerializedName("fees")
    val fees: JupiterSwapFeesRequest
)
