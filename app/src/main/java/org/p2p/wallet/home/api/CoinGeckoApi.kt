package org.p2p.wallet.home.api

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

typealias CurrencyToPrice = Map<String, Float>
typealias TokenIdsToPrices = Map<String, CurrencyToPrice>

interface CoinGeckoApi {
    /**
     * {
     *   bitcoin: {
     *     usd: 1111
     *   },
     *   ...
     * }
     */
    @GET("simple/price")
    suspend fun getTokenPrices(
        @Query("ids") tokenIds: String,
        @Query("vs_currencies") targetCurrency: String
    ): TokenIdsToPrices

    @GET("coins/list")
    suspend fun getAllTokens(): ResponseBody
}
