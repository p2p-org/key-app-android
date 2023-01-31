package org.p2p.wallet.user.repository.prices.impl

import kotlinx.coroutines.withContext
import org.p2p.wallet.home.api.CoinGeckoApi
import org.p2p.wallet.home.model.TokenPrice
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.user.repository.prices.TokenPricesRemoteRepository
import org.p2p.wallet.user.repository.prices.TokenSymbol

class TokenPricesCoinGeckoRepository(
    private val coinGeckoApi: CoinGeckoApi,
    private val dispatchers: CoroutineDispatchers
) : TokenPricesRemoteRepository {

    override suspend fun getTokenPricesBySymbols(
        tokenSymbols: List<TokenSymbol>,
        targetCurrency: String
    ): List<TokenPrice> = withContext(dispatchers.io) {
        loadPrices(tokenSymbols, targetCurrency)
    }

    override suspend fun getTokenPriceBySymbol(
        tokenSymbol: TokenSymbol,
        targetCurrency: String
    ): TokenPrice = withContext(dispatchers.io) {
        loadPrices(listOf(tokenSymbol), targetCurrency).first()
    }

    private suspend fun loadPrices(tokenSymbols: List<TokenSymbol>, targetCurrencySymbol: String): List<TokenPrice> {
        val tokenIdsForReqeust = tokenSymbols.joinToString(",") { it.symbol }
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
