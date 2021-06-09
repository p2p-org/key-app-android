package com.p2p.wallet.user.local

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
    val logoUrl: String?
)