package org.p2p.market.price.api.response

import com.google.gson.annotations.SerializedName

internal enum class NetworkChain {
    @SerializedName("solana")
    SOLANA,

    @SerializedName("ethereum")
    ETHEREUM
}
