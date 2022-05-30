package org.p2p.wallet.home.api

import com.google.gson.JsonObject
import retrofit2.http.GET
import retrofit2.http.Query

interface CompareApi {

    @GET("data/pricemulti")
    suspend fun getMultiPrice(
        @Query("fsyms") tokensFrom: String,
        @Query("tsyms") tokenTo: String
    ): JsonObject
}
