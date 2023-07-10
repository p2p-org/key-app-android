package org.p2p.wallet.user.repository.prices.impl

import retrofit2.HttpException
import timber.log.Timber
import java.math.BigDecimal
import kotlinx.coroutines.withContext
import org.p2p.core.model.TokenPriceWithMark
import org.p2p.core.pricecache.PriceCacheRepository
import org.p2p.core.utils.Constants
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.wallet.infrastructure.network.coingecko.CoinGeckoApi
import org.p2p.wallet.infrastructure.network.coingecko.response.CoinGeckoPriceResponse
import org.p2p.wallet.user.repository.prices.TokenCoinGeckoId
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository
import org.p2p.wallet.utils.ifNotEmpty

private const val ACCEPTABLE_RATE_DIFF = 0.02

private val STABLE_TOKENS_COINGECKO_IDS = setOf(
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
        tokenIds: List<TokenCoinGeckoId>,
        targetCurrency: String
    ): List<TokenPrice> = loadPrices(tokenIds, targetCurrency)

    override suspend fun getTokenPriceById(
        tokenId: TokenCoinGeckoId,
        targetCurrency: String
    ): TokenPrice = loadPrices(listOf(tokenId), targetCurrency).first { it.tokenId == tokenId.id }

    override suspend fun getTokenPricesByIdsMap(
        tokenIds: List<TokenCoinGeckoId>,
        targetCurrency: String
    ): Map<TokenCoinGeckoId, TokenPrice> =
        getTokenPriceByIds(tokenIds, targetCurrency).associateBy { TokenCoinGeckoId(it.tokenId) }

    private suspend fun loadPrices(
        tokenIds: List<TokenCoinGeckoId>,
        targetCurrencySymbol: String
    ): List<TokenPrice> = withContext(dispatchers.io) {
        priceCacheRepository.getExpiredPricesIds(tokenIds = tokenIds.map(TokenCoinGeckoId::id))
            .ifNotEmpty { fetchRemoteAndUpdateCache(tokenIds, targetCurrencySymbol) }

        priceCacheRepository.getPricesList()
            .map { TokenPrice(tokenId = it.key, price = it.value.priceInUsd) }
    }

    private suspend fun fetchRemoteAndUpdateCache(tokenIds: List<TokenCoinGeckoId>, targetCurrencySymbol: String) {
        try {
            val tokenIdsForRequest = tokenIds.joinToString(",", transform = TokenCoinGeckoId::id)
            val response: Map<String, TokenPriceWithMark> = coinGeckoApi.getTokenPrices(
                tokenIds = tokenIdsForRequest,
                targetCurrency = targetCurrencySymbol.lowercase()
            )
                .associateBy(
                    keySelector = CoinGeckoPriceResponse::id,
                    valueTransform = { TokenPriceWithMark(it.currentPrice) }
                )
                .swapStablePrices()

            priceCacheRepository.mergeCache(response)
        } catch (httpException: HttpException) {
            logHttpErrorCode(httpException)
            throw httpException
        }
    }

    private fun logHttpErrorCode(httpException: HttpException) {
        val errorCode = httpException.code()
        if (errorCode == 429 || errorCode == 403) {
            Timber.e(RequestRateLimitMet(httpException))
        }
    }

    private fun Map<String, TokenPriceWithMark>.swapStablePrices(): Map<String, TokenPriceWithMark> {
        val pricesMap = toMutableMap()
        STABLE_TOKENS_COINGECKO_IDS.forEach { tokenId ->
            pricesMap[tokenId]?.also { tokenPrice ->
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
