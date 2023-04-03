package org.p2p.ethereumkit.external.price

import java.math.BigDecimal
import org.p2p.core.model.TokenPriceWithMark
import org.p2p.core.pricecache.PriceCacheRepository
import org.p2p.core.utils.orZero
import org.p2p.ethereumkit.external.api.coingecko.CoinGeckoService
import org.p2p.ethereumkit.external.api.coingecko.response.CoinGeckoPriceResponse

internal class EthereumPriceRepository(
    private val priceApi: CoinGeckoService,
    private val priceCacheRepository: PriceCacheRepository,
) : PriceRepository {

    override suspend fun getPriceForTokens(
        tokenAddresses: List<String>,
        targetCurrency: String,
    ): Map<String, BigDecimal> {
        val localTokenPrices = priceCacheRepository.getPrices().map { it.value }
        if (!isTokenPriceValid(localTokenPrices)) {

            val result = loadPriceForEthereumToken(
                tokenAddress = tokenAddresses.joinToString(","),
                targetCurrency = targetCurrency
            ).let { mapToTokenPriceWithMark(it) }

            priceCacheRepository.mergeCache(result)
        }
        return priceCacheRepository.getPricesList()
            .associate { it.key to it.value.priceInUsd }
    }

    override suspend fun getPriceForToken(tokenAddress: String, targetCurrency: String): BigDecimal {
        val localTokenPrice = priceCacheRepository.getPriceByKey(tokenAddress)
        if (isTokenPriceValid(localTokenPrice)) {
            val result = loadPriceForEthereumToken(
                tokenAddress = tokenAddress,
                targetCurrency = targetCurrency
            )
            val newTokenPrice = TokenPriceWithMark(result[targetCurrency]?.priceInUsd.orZero())
            priceCacheRepository.setPriceByKey(key = tokenAddress, newTokenPrice)
        }
        return priceCacheRepository.getPriceByKey(tokenAddress)
            ?.priceInUsd
            .orZero()
    }

    private fun isTokenPriceValid(price: TokenPriceWithMark?): Boolean {
        return price != null && price.isValid()
    }

    private fun isTokenPriceValid(prices: List<TokenPriceWithMark>): Boolean {
        return prices.isNotEmpty() && prices.all { it.isValid() }
    }

    private fun mapToTokenPriceWithMark(items: Map<String, CoinGeckoPriceResponse?>): Map<String, TokenPriceWithMark> {
        return items.entries.associate { it.key to TokenPriceWithMark(it.value?.priceInUsd.orZero()) }
    }

    private suspend fun loadPriceForEthereumToken(
        tokenAddress: String,
        targetCurrency: String,
    ): Map<String, CoinGeckoPriceResponse> {
        return priceApi.getEthereumTokenPrices(
            tokenAddresses = tokenAddress,
            targetCurrency = targetCurrency
        )
    }
}
