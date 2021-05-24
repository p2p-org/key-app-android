package com.p2p.wallet.main.api

import retrofit2.http.GET
import retrofit2.http.Path

interface BonfidaApi {

    @GET("orderbooks/{tokenSymbol}")
    suspend fun getOrderBooks(
        @Path("tokenSymbol") symbol: String
    ): OrderBooksResponse
}