package org.p2p.ethereumkit.external.price

import java.math.BigDecimal
import java.time.ZonedDateTime
import org.p2p.ethereumkit.external.api.coingecko.CoinGeckoService

const val DEFAULT_EXPIRATION_TIME_SEC = 60

internal class EthereumPriceRepository(
    private val priceApi: CoinGeckoService
) : PriceRepository {

    private val cachedPrices: MutableMap<String, TokenPrice> = mutableMapOf()

    override suspend fun getTokenPrices(
        tokenAddresses: List<String>,
        targetCurrency: String
    ): Map<String, BigDecimal> {
        val filteredTokenAddresses = filterAddressesForExpiredPrices(tokenAddresses)
        if (filteredTokenAddresses.isNotEmpty()) {
            val response = priceApi.getEthereumTokenPrices(
                tokenAddresses = filteredTokenAddresses.joinToString(","),
                targetCurrency = targetCurrency
            )
            val timestamp = ZonedDateTime.now().toEpochSecond()
            mergeCache(response.entries.associate { it.key to TokenPrice(it.value.priceInUsd, timestamp) })
        }
        return cachedPrices.entries.associate { it.key to it.value.priceInUsd }
    }

    private fun filterAddressesForExpiredPrices(tokenAddresses: List<String>): List<String> {
        return tokenAddresses.filterNot { cachedPrices[it]?.isValid() == true }
    }

    private fun mergeCache(newItems: Map<String, TokenPrice>) {
        for ((k, v) in newItems) {
            cachedPrices.merge(k, v) { _, newVal -> newVal }
        }
    }
}

internal class TokenPrice(
    val priceInUsd: BigDecimal,
    val timestamp: Long
) {
    fun isValid(): Boolean {
        return ZonedDateTime.now().toEpochSecond() - timestamp < DEFAULT_EXPIRATION_TIME_SEC
    }
}
