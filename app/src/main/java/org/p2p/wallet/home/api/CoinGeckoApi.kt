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

    @GET("coins/list")
    suspend fun getAllTokens(): ResponseBody
}
