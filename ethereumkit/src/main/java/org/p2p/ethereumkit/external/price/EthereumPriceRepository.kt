package org.p2p.ethereumkit.external.price

import java.math.BigDecimal
import org.p2p.core.model.TokenPriceWithMark
import org.p2p.core.pricecache.PriceCacheRepository
import org.p2p.ethereumkit.external.api.coingecko.CoinGeckoService

internal class EthereumPriceRepository(
    private val priceApi: CoinGeckoService
) : PriceRepository, PriceCacheRepository() {

    override suspend fun getTokenPrices(
        tokenAddresses: List<String>,
        targetCurrency: String
    ): Map<String, BigDecimal> {
        val filteredTokenAddresses = filterKeysForExpiredPrices(tokenAddresses)
        if (filteredTokenAddresses.isNotEmpty()) {
            val response = priceApi.getEthereumTokenPrices(
                tokenAddresses = filteredTokenAddresses.joinToString(","),
                targetCurrency = targetCurrency
            )
            mergeCache(response.entries.associate { it.key to TokenPriceWithMark(it.value.priceInUsd) })
        }
        return cachedPrices.entries.associate { it.key to it.value.priceInUsd }
    }
}
