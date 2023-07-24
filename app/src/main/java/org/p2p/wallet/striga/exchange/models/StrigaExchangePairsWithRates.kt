package org.p2p.wallet.striga.exchange.models

private typealias StrigaExchangePair = Pair<String, String>

data class StrigaExchangePairsWithRates(
    private val rateMap: Map<StrigaExchangePair, StrigaExchangeRate>
) {

    fun getAvailablePairsForToken(tokenSymbol: String): Set<StrigaExchangePair> {
        return rateMap.keys.filter { it.hasValue(tokenSymbol) }.toSet()
    }

    fun hasRate(fromTokenSymbol: String, toTokenSymbol: String): Boolean {
        return rateMap.containsKey(StrigaExchangePair(fromTokenSymbol, toTokenSymbol)) ||
            rateMap.containsKey(StrigaExchangePair(toTokenSymbol, fromTokenSymbol))
    }

    fun findRate(fromTokenSymbol: String, toTokenSymbol: String): StrigaExchangeRate? {
        return rateMap[StrigaExchangePair(fromTokenSymbol, toTokenSymbol)]
            ?: rateMap[StrigaExchangePair(toTokenSymbol, fromTokenSymbol)]
    }

    private fun Pair<String, String>.hasValue(value: String): Boolean {
        return first.equals(value, true) || second.equals(value, true)
    }
}
