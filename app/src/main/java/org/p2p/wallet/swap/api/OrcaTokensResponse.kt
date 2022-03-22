package org.p2p.wallet.swap.api

import com.google.gson.annotations.SerializedName

data class OrcaTokensResponse(
    @SerializedName("mint")
    val mint: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("decimals")
    val decimals: Int,
    @SerializedName("fetchPrice")
    val fetchPrice: Boolean,
)
