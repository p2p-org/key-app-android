package org.p2p.wallet.jupiter.api.response.tokens

import com.google.gson.annotations.SerializedName

data class JupiterTokenExtensions(
    @SerializedName("coingeckoId")
    val coingeckoId: String?
)
