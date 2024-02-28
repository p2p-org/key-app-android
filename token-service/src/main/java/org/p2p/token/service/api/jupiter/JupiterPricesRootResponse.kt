package org.p2p.token.service.api.jupiter

import com.google.gson.annotations.SerializedName

internal class JupiterPricesRootResponse(
    @SerializedName("data")
    val tokenMintsToPrices: Map<String, JupiterPricesResponse>
)

internal class JupiterPricesResponse(
    @SerializedName("id")
    val mintAddress: String,
    @SerializedName("mintSymbol")
    val tokenSymbol: String,
    // 1 unit of the token worth in USDC
    @SerializedName("price")
    val usdPrice: String
)
