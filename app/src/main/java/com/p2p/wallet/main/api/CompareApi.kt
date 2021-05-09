package com.p2p.wallet.main.api

import retrofit2.http.GET
import retrofit2.http.Query

interface CompareApi {

    @GET("data/pricemulti")
    suspend fun getMultiPrice(
        @Query("fsyms") tokensFrom: String,
        @Query("tsyms") tokenTo: String
    ): TokenPriceResponse

    @GET("data/price")
    suspend fun getPrice(
        @Query("fsym") tokenFrom: String,
        @Query("tsyms") tokenTo: String,
    ): PriceResponse
}