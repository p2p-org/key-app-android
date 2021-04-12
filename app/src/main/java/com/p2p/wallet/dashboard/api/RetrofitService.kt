package com.p2p.wallet.dashboard.api

import com.p2p.wallet.common.network.CallRequest
import com.p2p.wallet.common.network.HistoricalPrices
import com.p2p.wallet.common.network.ResponceDataBonfida
import com.p2p.wallet.common.network.ResponseData
import com.p2p.wallet.common.network.ResponseDataAirDrop
import com.p2p.wallet.dashboard.model.orderbook.OrderBooks
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

@Deprecated("Separate requests by servers")
interface RetrofitService {
    @Headers("Content-Type: application/json")
    @POST(".")
    suspend fun getBalance(@Body jsonObject: CallRequest): Response<ResponseData>

    @Headers("Content-Type: application/json")
    @POST(".")
    suspend fun requestAirdrop(@Body jsonObject: CallRequest): Response<ResponseDataAirDrop>

    @GET("orderbooks/{tokenSymbol}")
    suspend fun getOrderBooks(@Path("tokenSymbol") symbol: String): Response<ResponceDataBonfida<OrderBooks>>

    @GET("candles/{tokenSymbol}")
    suspend fun getHistoricalPrices(
        @Path("tokenSymbol") symbol: String,
        @Query("resolution") resolution: Int,
        @Query("startTime") startTime: Long,
        @Query("endTime") endTime: Long,
    ): Response<ResponceDataBonfida<List<HistoricalPrices>>>

    @GET("candles/{tokenSymbol}")
    suspend fun getHistoricalPrices(
        @Path("tokenSymbol") symbol: String,
        @Query("limit") limit: Int,
        @Query("resolution") resolution: Int
    ): Response<ResponceDataBonfida<List<HistoricalPrices>>>
    @GET("candles/{tokenSymbol}")
    suspend fun getAllHistoricalPrices(
        @Path("tokenSymbol") symbol: String,
        @Query("resolution") resolution: Int
    ): Response<ResponceDataBonfida<List<HistoricalPrices>>>
}