package org.p2p.wallet.user.repository.prices.impl

import kotlinx.coroutines.withContext
import org.p2p.wallet.home.api.CoinGeckoApi
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.user.repository.prices.TokenId
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository

class TokenPricesCoinGeckoRepository(
    private val coinGeckoApi: CoinGeckoApi,
    private val dispatchers: CoroutineDispatchers
) : TokenPricesRemoteRepository {

    override suspend fun getTokenPriceByIds(
        tokenIds: List<TokenId>,
        targetCurrency: String
    ): List<TokenPrice> = loadPrices(tokenIds, targetCurrency)

    override suspend fun getTokenPriceById(
        tokenId: TokenId,
        targetCurrency: String
    ): TokenPrice = loadPrices(listOf(tokenId), targetCurrency).first()

    override suspend fun getTokenPricesByIdsMap(
        tokenIds: List<TokenId>,
        targetCurrency: String
    ): Map<TokenId, TokenPrice> = getTokenPriceByIds(tokenIds, targetCurrency).associateBy { TokenId(it.tokenId) }

    private suspend fun loadPrices(tokenIds: List<TokenId>, targetCurrencySymbol: String): List<TokenPrice> =
        withContext(dispatchers.io) {
            val tokenIdsForRequest = tokenIds.joinToString(",") { it.id }
            coinGeckoApi.getTokenPrices(
                tokenIds = tokenIdsForRequest,
                targetCurrency = targetCurrencySymbol.lowercase()
            )
                .map { TokenPrice(tokenId = it.id, price = it.currentPrice) }
        }
}
