package org.p2p.wallet.swap.api

import com.google.gson.annotations.SerializedName

class OrcaCollectiblesResponse(
    @SerializedName("mint")
    val mint: String,
    @SerializedName("decimals")
    val decimals: Int
)
