package org.p2p.market.price.api.request

import com.google.gson.annotations.SerializedName
import org.p2p.market.price.api.response.NetworkChain

internal data class MarketPriceItemRequest(
    @SerializedName("chain_id")
    val chainId: NetworkChain,
    @SerializedName("addresses")
    val addresses: List<String>
)
