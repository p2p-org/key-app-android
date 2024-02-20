package org.p2p.token.service.api.coingecko

import retrofit2.http.GET
import retrofit2.http.Query
import kotlinx.coroutines.withContext
import org.p2p.core.dispatchers.CoroutineDispatchers
import org.p2p.token.service.api.coingecko.response.CoinGeckoEthereumPriceResponse
import org.p2p.token.service.api.coingecko.response.CoinGeckoSolPriceResponse
import org.p2p.token.service.model.TokenRate
import org.p2p.token.service.model.TokenServiceNetwork
import org.p2p.token.service.model.TokenServicePrice

internal class CoinGeckoTokenPriceRepository(
    private val api: CoinGeckoDataSource,
    private val dispatchers: CoroutineDispatchers
) {
    private var cachedAddressesToPrices = mutableMapOf<String, TokenServicePrice>()

    suspend fun loadEthereumTokenPrices(
        tokenAddresses: List<String>
    ): List<TokenServicePrice> = withContext(dispatchers.io) {
        val cachedPrices = tokenAddresses.mapNotNull { cachedAddressesToPrices[it] }
        if (cachedPrices.size == tokenAddresses.size) {
            return@withContext cachedPrices
        }

        val request = tokenAddresses.joinToString(",")
        val currency = "usd"
        val response = api.getEthereumTokenPrices(request, currency).mapValues {
            TokenServicePrice(
                tokenAddress = it.key,
                rate = TokenRate(it.value.priceInUsd),
                network = TokenServiceNetwork.ETHEREUM
            )
        }
        cachedAddressesToPrices.putAll(response)
        response.values.toList()
    }
}

internal interface CoinGeckoDataSource {
    @GET("coins/markets")
    suspend fun getTokenPrices(
        @Query("ids") tokenIds: String,
        @Query("vs_currency") targetCurrency: String
    ): List<CoinGeckoSolPriceResponse>

    @GET("simple/token_price/ethereum")
    suspend fun getEthereumTokenPrices(
        @Query("contract_addresses", encoded = true) tokenAddresses: String,
        @Query("vs_currencies") targetCurrency: String
    ): Map<String, CoinGeckoEthereumPriceResponse>
}
