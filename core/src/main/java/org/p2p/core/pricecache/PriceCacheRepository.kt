package org.p2p.core.pricecache

import org.p2p.core.model.TokenPriceWithMark

class PriceCacheRepository {

    private val cachedPrices: MutableMap<String, TokenPriceWithMark> = mutableMapOf()

    fun getPrices(): Map<String, TokenPriceWithMark> = cachedPrices

    fun getPricesList(): Set<Map.Entry<String, TokenPriceWithMark>> = cachedPrices.entries

    fun getPriceByKey(key: String): TokenPriceWithMark? {
        return cachedPrices[key]
    }

    fun setPriceByKey(key: String, value: TokenPriceWithMark) {
        cachedPrices[key] = value
    }

    fun getExpiredPricesIds(tokenIds: List<String>): List<String> {
        return tokenIds.filterNot { cachedPrices[it]?.isValid() == true }
    }

    fun mergeCache(newItems: Map<String, TokenPriceWithMark>) {
        cachedPrices.putAll(newItems)
    }
}
