package org.p2p.wallet.swap.jupiter.repository.model

import com.google.gson.annotations.SerializedName
import org.p2p.wallet.utils.Base58String
import java.math.BigDecimal

data class SwapMarketInformation(
    @SerializedName("id")
    val id: String,
    @SerializedName("label")
    val label: String,
    @SerializedName("inputMint")
    val inputMint: Base58String,
    @SerializedName("outputMint")
    val outputMint: Base58String,
    @SerializedName("notEnoughLiquidity")
    val notEnoughLiquidity: Boolean,
    @SerializedName("inAmount")
    val inAmount: BigDecimal,
    @SerializedName("outAmount")
    val outAmount: BigDecimal,
    @SerializedName("minInAmount")
    val minInAmount: BigDecimal?,
    @SerializedName("minOutAmount")
    val minOutAmount: BigDecimal?,
    @SerializedName("priceImpactPct")
    val priceImpactPct: Int,
    @SerializedName("lpFee")
    val lpFee: LpFee,
    @SerializedName("platformFee")
    val platformFee: PlatformFeeRequest
) {
    data class LpFee(
        @SerializedName("amount")
        val amount: String,
        @SerializedName("mint")
        val mint: Base58String,
        @SerializedName("pct")
        val pct: Int
    )

    data class PlatformFeeRequest(
        @SerializedName("amount")
        val amount: String,
        @SerializedName("mint")
        val mint: Base58String,
        @SerializedName("pct")
        val pct: Int
    )
}
