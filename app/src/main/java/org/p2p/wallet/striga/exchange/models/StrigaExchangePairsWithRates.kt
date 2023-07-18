package org.p2p.wallet.striga.exchange.models

import java.math.BigDecimal
import org.p2p.core.utils.MillisSinceEpoch

private typealias StrigaExchangePair = Pair<String, String>

data class StrigaExchangePairsWithRates(
    private val rateMap: Map<StrigaExchangePair, Rate>
) {
    data class Rate(
        val price: BigDecimal,
        val buyRate: BigDecimal,
        val sellRate: BigDecimal,
        val timestamp: MillisSinceEpoch,
        val currency: String,
    )

    fun getAvailablePairsForToken(tokenSymbol: String): Set<StrigaExchangePair> {
        return rateMap.keys.filter { it.hasValue(tokenSymbol) }.toSet()
    }

    fun hasRate(fromTokenSymbol: String, toTokenSymbol: String): Boolean {
        return rateMap.containsKey(StrigaExchangePair(fromTokenSymbol, toTokenSymbol)) ||
            rateMap.containsKey(StrigaExchangePair(toTokenSymbol, fromTokenSymbol))
    }

    fun findRate(fromTokenSymbol: String, toTokenSymbol: String): Rate? {
        return rateMap[StrigaExchangePair(fromTokenSymbol, toTokenSymbol)]
            ?: rateMap[StrigaExchangePair(toTokenSymbol, fromTokenSymbol)]
    }

    private fun Pair<String, String>.hasValue(value: String): Boolean {
        return first.equals(value, true) || second.equals(value, true)
    }
}
