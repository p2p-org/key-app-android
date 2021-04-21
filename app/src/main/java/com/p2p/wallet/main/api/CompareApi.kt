package com.p2p.wallet.main.api

import retrofit2.http.GET
import retrofit2.http.Query

interface CompareApi {

    @GET("data/price")
    suspend fun getUSPrice(
        @Query("fsym") tokenFrom: String,
        @Query("tsyms") tokenTo: String,
    ): PriceDollarResponse
}