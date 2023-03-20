package org.p2p.wallet.home.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinGeckoApi {
    /**
     * {
     *   bitcoin: {
     *     usd: 1111
     *   },
     *   ...
     * }
     */
    @GET("coins/markets")
    suspend fun getTokenPrices(
        @Query("ids") tokenIds: String,
        @Query("vs_currency") targetCurrency: String
    ): List<CoinGeckoPriceResponse>

    @GET("simple/token_price/ethereum")
    suspend fun getEthereumTokenPrices(
        @Query("contract_addresses") tokenAddresses: String,
        @Query("vs_currencies") targetCurrency: String
    ): Map<String, CoinGeckoEthereumPrice>

    @GET("coins/list")
    suspend fun getAllTokens(): ResponseBody
}
