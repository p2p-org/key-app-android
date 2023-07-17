package org.p2p.wallet.striga.exchange.repository.mapper

import java.math.BigDecimal
import org.p2p.core.utils.Constants
import org.p2p.wallet.striga.exchange.api.response.StrigaExchangeRateItemResponse
import org.p2p.wallet.striga.exchange.models.StrigaExchangePairsWithRates

class StrigaExchangeRepositoryMapper {

    companion object {
        private val SUPPORTED_TOKEN_RATES = setOf(
            Constants.USDC_SYMBOL,
            Constants.USDT_SYMBOL,
            Constants.ETH_SYMBOL,
            Constants.EUR_SYMBOL,
            Constants.BTC_SYMBOL,
        )
    }

    fun fromNetwork(response: Map<String, StrigaExchangeRateItemResponse>): StrigaExchangePairsWithRates {
        // all pairs are presented as "CRYPTOFIAT" with no any separator
        // FIAT is also could be a BUSD, USDC or EUR, so we need to find them all
        val rateMap = mutableMapOf<Pair<String, String>, StrigaExchangePairsWithRates.Rate>()
        response.forEach { (pair, rate) ->
            val token1 = SUPPORTED_TOKEN_RATES.find { pair.startsWith(it) }
            val token2 = SUPPORTED_TOKEN_RATES.find { pair.endsWith(it) }

            if (token1 != null && token2 != null) {
                rateMap[token1 to token2] = rate.toDomain()
            }
        }
        return StrigaExchangePairsWithRates(rateMap)
    }

    private fun StrigaExchangeRateItemResponse.toDomain(): StrigaExchangePairsWithRates.Rate =
        StrigaExchangePairsWithRates.Rate(
            price = BigDecimal(price),
            buy = BigDecimal(buy),
            sell = BigDecimal(sell),
            timestamp = timestamp,
            currency = currency,
        )
}
