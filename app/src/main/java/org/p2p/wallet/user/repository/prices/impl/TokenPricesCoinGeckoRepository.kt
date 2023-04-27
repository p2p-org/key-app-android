package org.p2p.wallet.user.repository.prices.impl

import retrofit2.HttpException
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.withContext
import org.p2p.core.model.TokenPriceWithMark
import org.p2p.core.pricecache.PriceCacheRepository
import org.p2p.core.utils.Constants
import org.p2p.wallet.home.api.CoinGeckoApi
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.user.repository.prices.TokenId
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository

private const val ACCEPTABLE_RATE_DIFF = 0.02

val STABLE_TOKENS_COINGECKO_IDS = setOf(
    Constants.USDT_COINGECKO_ID,
    Constants.USDC_COINGECKO_ID
)

class TokenPricesCoinGeckoRepository(
    private val coinGeckoApi: CoinGeckoApi,
    private val priceCacheRepository: PriceCacheRepository,
    private val dispatchers: CoroutineDispatchers
) : TokenPricesRemoteRepository {

    private class RequestRateLimitMet(cause: HttpException) : Throwable(
        message = "Rate limit for coin_gecko met",
        cause = cause
    )

    override suspend fun getTokenPriceByIds(
        tokenIds: List<TokenId>,
        targetCurrency: String
    ): List<TokenPrice> = loadPrices(tokenIds, targetCurrency)

    override suspend fun getTokenPriceById(
        tokenId: TokenId,
        targetCurrency: String
    ): TokenPrice = loadPrices(listOf(tokenId), targetCurrency).first { it.tokenId == tokenId.id }

    override suspend fun getTokenPricesByIdsMap(
        tokenIds: List<TokenId>,
        targetCurrency: String
    ): Map<TokenId, TokenPrice> = getTokenPriceByIds(tokenIds, targetCurrency).associateBy { TokenId(it.tokenId) }

    private suspend fun loadPrices(tokenIds: List<TokenId>, targetCurrencySymbol: String): List<TokenPrice> =
        withContext(dispatchers.io) {
            val filteredTokenIds = priceCacheRepository.filterKeysForExpiredPrices(tokenIds.map { it.id })
            if (filteredTokenIds.isNotEmpty()) {
                val tokenIdsForRequest = tokenIds.joinToString(",") { it.id }
                try {
                    val response = coinGeckoApi.getTokenPrices(
                        tokenIds = tokenIdsForRequest,
                        targetCurrency = targetCurrencySymbol.lowercase()
                    ).associate { it.id to TokenPriceWithMark(it.currentPrice) }
                    priceCacheRepository.mergeCache(response.swapStablePrices())
                } catch (httpException: HttpException) {
                    val errorCode = httpException.code()
                    if (errorCode == 429 || errorCode == 403) {
                        Timber.e(RequestRateLimitMet(httpException))
                    }
                    throw httpException
                }
            }
            priceCacheRepository.getPricesList().map { TokenPrice(tokenId = it.key, price = it.value.priceInUsd) }
        }

    private fun Map<String, TokenPriceWithMark>.swapStablePrices(): Map<String, TokenPriceWithMark> {
        val pricesMap = toMutableMap()
        STABLE_TOKENS_COINGECKO_IDS.forEach { tokenId ->
            pricesMap[tokenId]?.let { tokenPrice ->
                if (isStableCoinRateDiffAcceptable(tokenPrice.priceInUsd)) {
                    pricesMap[tokenId] = TokenPriceWithMark(
                        priceInUsd = BigDecimal.ONE,
                        timestamp = tokenPrice.timestamp
                    )
                }
            }
        }
        return pricesMap
    }

    private fun isStableCoinRateDiffAcceptable(priceInUsd: BigDecimal): Boolean {
        val delta = priceInUsd - BigDecimal.ONE
        return delta.abs() < BigDecimal(ACCEPTABLE_RATE_DIFF)
    }
}
