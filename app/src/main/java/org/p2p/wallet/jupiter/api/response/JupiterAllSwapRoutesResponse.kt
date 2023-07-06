package org.p2p.wallet.jupiter.api.response

import com.google.gson.annotations.SerializedName
import org.p2p.core.crypto.Base58String

data class JupiterAllSwapRoutesResponse(
    @SerializedName("mintKeys")
    val mintKeys: List<Base58String>,
    @SerializedName("indexedRouteMap")
    val routeMap: Map<String, List<Int>>
)
