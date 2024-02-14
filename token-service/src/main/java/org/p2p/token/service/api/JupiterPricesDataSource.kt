package org.p2p.token.service.api

import retrofit2.http.GET
import retrofit2.http.Query

internal interface JupiterPricesDataSource {
    @GET("v4/price")
    suspend fun getPrices(@Query("ids", encoded = true) tokenMints: String): JupiterPricesRootResponse
}
