package org.p2p.wallet.feerelayer.api

import com.google.gson.annotations.SerializedName

class SwapDataRequest(
    @SerializedName("Spl")
    val spl: SwapSplRequest?,
    @SerializedName("SplTransitive")
    val splTransitive: SwapSplTransitiveRequest?
)
