package org.p2p.wallet.user.repository.prices.impl

import kotlinx.coroutines.withContext
import org.p2p.wallet.home.api.CoinGeckoApi
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository
import org.p2p.wallet.user.repository.prices.TokenId

class TokenPricesCoinGeckoRepository(
    private val coinGeckoApi: CoinGeckoApi,
    private val dispatchers: CoroutineDispatchers
) : TokenPricesRemoteRepository {

    override suspend fun getTokenPricesBySymbols(
        tokenSymbols: List<TokenId>,
        targetCurrency: String
    ): List<TokenPrice> = withContext(dispatchers.io) {
        loadPrices(tokenSymbols, targetCurrency)
    }

    override suspend fun getTokenPriceBySymbol(
        tokenSymbol: TokenId,
        targetCurrency: String
    ): TokenPrice = withContext(dispatchers.io) {
        loadPrices(listOf(tokenSymbol), targetCurrency).first()
    }

    private suspend fun loadPrices(tokenSymbols: List<TokenId>, targetCurrencySymbol: String): List<TokenPrice> {
        val tokenIdsForReqeust = tokenSymbols.joinToString(",") { it.id }
        return coinGeckoApi.getTokenPrices(
            tokenIds = tokenIdsForReqeust,
            targetCurrency = targetCurrencySymbol.lowercase()
        )
            .map {
                TokenPrice(
                    tokenSymbol = it.id,
                    price = it.currentPrice
                )
            }
    }
}
