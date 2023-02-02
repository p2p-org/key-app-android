package org.p2p.wallet.swap.jupiter.api.response.quote

import com.google.gson.annotations.SerializedName

data class SwapJupiterQuoteResponse(
    @SerializedName("data")
    val routes: List<JupiterRouteResponse>
)
