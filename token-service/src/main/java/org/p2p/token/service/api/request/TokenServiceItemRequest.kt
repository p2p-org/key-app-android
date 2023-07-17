package org.p2p.token.service.api.request

import com.google.gson.annotations.SerializedName
import org.p2p.token.service.api.response.TokenServiceNetworkResponse

internal data class TokenServiceItemRequest(
    @SerializedName("chain_id")
    val chainId: TokenServiceNetworkResponse,
    @SerializedName("addresses")
    val addresses: List<String>
)
