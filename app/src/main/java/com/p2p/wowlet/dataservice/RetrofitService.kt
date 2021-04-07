package com.p2p.wowlet.dataservice


import com.p2p.wowlet.entities.responce.CallRequest
import com.p2p.wowlet.entities.responce.HistoricalPrices
import com.p2p.wowlet.entities.responce.ResponceDataBonfida
import com.p2p.wowlet.entities.responce.ResponseData
import com.p2p.wowlet.entities.responce.ResponseDataAirDrop
import com.p2p.wowlet.entities.responce.orderbook.OrderBooks
import retrofit2.Response
import retrofit2.http.*

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