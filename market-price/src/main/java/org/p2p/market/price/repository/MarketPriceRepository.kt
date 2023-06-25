package org.p2p.market.price.repository

import org.p2p.core.rpc.JsonRpc
import org.p2p.market.price.model.MarketPriceResult

interface MarketPriceRepository {
    suspend fun <P, T> launch(request: JsonRpc<P, T>): MarketPriceResult.Success<T>
}
