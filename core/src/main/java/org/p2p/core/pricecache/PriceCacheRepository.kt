package org.p2p.core.pricecache

import java.math.BigDecimal
import org.p2p.core.model.TokenPriceWithMark

class PriceCacheRepository() {

    private val cachedPrices: MutableMap<String, TokenPriceWithMark> = mutableMapOf()

    fun getPrices(): Map<String, TokenPriceWithMark> = cachedPrices

    fun getPriceByKey(key: String): BigDecimal? {
        return cachedPrices[key]?.priceInUsd
    }

    fun filterKeysForExpiredPrices(tokenKeys: List<String>): List<String> {
        return tokenKeys.filterNot { cachedPrices[it]?.isValid() == true }
    }

    fun mergeCache(newItems: Map<String, TokenPriceWithMark>) {
        for ((k, v) in newItems) {
            cachedPrices.merge(k, v) { _, newVal -> newVal }
        }
    }
}
