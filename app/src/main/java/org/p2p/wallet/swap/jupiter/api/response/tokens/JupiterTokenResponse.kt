package org.p2p.wallet.swap.jupiter.api.response.tokens

import com.google.gson.annotations.SerializedName

data class JupiterTokenResponse(
    @SerializedName("address")
    val address: String,
    @SerializedName("chainId")
    val chainId: Int,
    @SerializedName("decimals")
    val decimals: Int,
    @SerializedName("extensions")
    val extensions: JupiterTokenExtensions,
    @SerializedName("name")
    val name: String,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("tags")
    val tags: List<String>
)
