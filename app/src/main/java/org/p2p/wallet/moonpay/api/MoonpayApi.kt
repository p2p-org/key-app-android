package org.p2p.wallet.moonpay.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MoonpayApi {

    @GET("currencies/{currencyCode}/buy_quote/")
    suspend fun getBuyCurrency(
        @Path("currencyCode") quoteCurrencyCode: String,
        @Query("apiKey") apiKey: String,
        @Query("baseCurrencyAmount") baseCurrencyAmount: String?,
        @Query("quoteCurrencyAmount") quoteCurrencyAmount: String?,
        @Query("baseCurrencyCode") baseCurrencyCode: String,
    ): MoonpayBuyCurrencyResponse

    @GET("currencies/{currencyCode}/ask_price/")
    suspend fun getCurrencyAskPrice(
        @Path("currencyCode") quoteCurrencyCode: String,
        @Query("apiKey") apiKey: String
    ): MoonpayCurrencyResponse
}
