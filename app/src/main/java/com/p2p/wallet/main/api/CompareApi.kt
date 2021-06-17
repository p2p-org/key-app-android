package com.p2p.wallet.main.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Query

interface CompareApi {

    @GET("data/pricemulti")
    suspend fun getMultiPrice(
        @Query("fsyms") tokensFrom: String,
        @Query("tsyms") tokenTo: String
    ): MultiPriceResponse

    @GET("data/price")
    suspend fun getPrice(
        @Query("fsym") tokenFrom: String,
        @Query("tsyms") tokenTo: String,
    ): SinglePriceResponse

    @GET("data/v2/histoday")
    suspend fun getDailyPriceHistory(
        @Query("fsym") sourceToken: String,
        @Query("tsym") destination: String,
        @Query("aggregate") days: Int
    ): PriceHistoryResponse

    @GET("data/v2/histohour")
    suspend fun getHourlyPriceHistory(
        @Query("fsym") sourceToken: String,
        @Query("tsym") destination: String,
        @Query("aggregate") hours: Int,
        @Query("limit") limit: Int
    ): PriceHistoryResponse
}