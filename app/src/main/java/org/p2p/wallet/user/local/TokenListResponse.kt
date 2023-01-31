package org.p2p.wallet.user.local

import com.google.gson.annotations.SerializedName

data class TokenListResponse(

    @SerializedName("tokens")
    val tokens: List<TokenResponse>
)

data class TokenResponse(
    @SerializedName("address")
    val address: String,
    @SerializedName("symbol")
    val symbol: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("decimals")
    val decimals: Int,
    @SerializedName("logoURI")
    val logoUrl: String?,
    @SerializedName("tags")
    val tags: List<String>?,
    @SerializedName("extensions")
    val extensions: TokenExtensions?
) {

    companion object {
        private const val WRAPPED_TAG = "wrapped-sollet"
    }

    fun isWrapped() = !tags.isNullOrEmpty() && tags.any { it == WRAPPED_TAG }
}

data class TokenExtensions(
    @SerializedName("serumV3Usdc")
    val serumV3Usdc: String?,
    @SerializedName("serumV3Usdt")
    val serumV3Usdt: String?,
    @SerializedName("coingeckoId")
    val coingeckoId: String?
)
