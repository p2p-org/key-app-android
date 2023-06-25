package org.p2p.market.price.api.request

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import org.p2p.core.rpc.JsonRpc
import org.p2p.market.price.api.response.MarketPriceQueryResponse

internal data class MarketPriceRequest(
    @Transient val marketRequest: List<MarketPriceItemRequest>
) : JsonRpc<List<MarketPriceItemRequest>, List<MarketPriceQueryResponse>>(
    method = "get_token_price",
    params = marketRequest
) {
    @Transient
    override val typeOfResult: Type = object : TypeToken<List<MarketPriceQueryResponse>>() {}.type
}
