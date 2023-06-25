package org.p2p.market.price.model

sealed class  MarketPriceResult {
    data class Success<T>(val data: T): MarketPriceResult()

    sealed class Error: MarketPriceResult()
}
