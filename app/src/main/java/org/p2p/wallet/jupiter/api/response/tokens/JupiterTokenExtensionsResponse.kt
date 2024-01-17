package org.p2p.wallet.jupiter.api.response.tokens

import com.google.gson.annotations.SerializedName

data class JupiterTokenExtensionsResponse(
    @SerializedName("coingeckoId")
    val coingeckoId: String?
)
