package org.p2p.wallet.striga.exchange.repository.mapper

import org.p2p.core.utils.Constants
import org.p2p.wallet.striga.exchange.api.response.StrigaExchangeRateItemResponse
import org.p2p.wallet.striga.exchange.models.StrigaExchangePairsWithRates
import org.p2p.wallet.striga.exchange.models.StrigaExchangeRate

class StrigaExchangeRepositoryMapper {

    private companion object {
        private val SUPPORTED_TOKEN_RATES = setOf(
            Constants.USDC_SYMBOL,
            Constants.USDT_SYMBOL,
            Constants.ETH_SYMBOL,
            Constants.EUR_READABLE_SYMBOL,
            Constants.BTC_SYMBOL,
        )
    }

    fun fromNetwork(response: Map<String, StrigaExchangeRateItemResponse>): StrigaExchangePairsWithRates {
        // all pairs are presented as "CRYPTOFIAT" with no any separator
        // FIAT is also could be a BUSD, USDC or EUR, so we need to find them all
        val rateMap = mutableMapOf<Pair<String, String>, StrigaExchangeRate>()
        response.forEach { (pair, rate) ->
            val token1 = SUPPORTED_TOKEN_RATES.find { pair.startsWith(it) }
            val token2 = SUPPORTED_TOKEN_RATES.find { pair.endsWith(it) }

            if (token1 != null && token2 != null) {
                rateMap[token1 to token2] = rate.toDomain()
            }
        }
        return StrigaExchangePairsWithRates(rateMap)
    }

    private fun StrigaExchangeRateItemResponse.toDomain(): StrigaExchangeRate =
        StrigaExchangeRate(
            priceUsd = price.toBigDecimal(),
            buyRate = buyRate.toBigDecimal(),
            sellRate = sellRate.toBigDecimal(),
            timestamp = timestamp,
            currencyName = currency,
        )
}
