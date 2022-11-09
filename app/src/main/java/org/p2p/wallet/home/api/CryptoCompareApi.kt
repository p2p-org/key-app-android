package org.p2p.wallet.home.api

import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoCompareApi {
    @GET("data/price")
    suspend fun getPrice(
        @Query("api_key") apiKey: String,
        @Query("fsym") tokenFrom: String,
        @Query("tsyms") tokenTo: String
    ): JsonObject

    @GET("data/pricemulti")
    suspend fun getMultiPrice(
        @Query("api_key") apiKey: String,
        @Query("fsyms") tokensFrom: String,
        @Query("tsyms") tokenTo: String
    ): JsonObject
}
