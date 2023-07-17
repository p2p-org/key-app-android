package org.p2p.wallet.striga.exchange.models

import java.math.BigDecimal

private typealias StrigaExchangePair = Pair<String, String>

data class StrigaExchangePairsWithRates(
    private val rateMap: Map<StrigaExchangePair, Rate>
) {
    data class Rate(
        val price: BigDecimal,
        val buy: BigDecimal,
        val sell: BigDecimal,
        val timestamp: Long,
        val currency: String,
    )

    fun getAvailablePairsForToken(token: String): Set<StrigaExchangePair> {
        return rateMap.keys.filter { it.hasValue(token) }.toSet()
    }

    fun hasRate(from: String, to: String): Boolean {
        return rateMap.containsKey(StrigaExchangePair(from, to)) ||
            rateMap.containsKey(StrigaExchangePair(to, from))
    }

    fun findRate(from: String, to: String): Rate? {
        return rateMap[StrigaExchangePair(from, to)] ?: rateMap[StrigaExchangePair(to, from)]
    }

    private fun Pair<String, String>.hasValue(value: String): Boolean {
        return first.equals(value, true) || second.equals(value, true)
    }
}
