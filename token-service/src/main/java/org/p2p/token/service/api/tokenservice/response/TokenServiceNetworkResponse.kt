package org.p2p.token.service.api.tokenservice.response

import com.google.gson.annotations.SerializedName

internal enum class TokenServiceNetworkResponse {
    @SerializedName("solana")
    SOLANA,

    @SerializedName("ethereum")
    ETHEREUM
}
