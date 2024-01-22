package org.p2p.wallet.jupiter.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.math.BigInteger
import org.p2p.wallet.jupiter.api.request.CreateSwapTransactionV6Request
import org.p2p.wallet.jupiter.api.response.CreateSwapTransactionV6Response
import org.p2p.wallet.jupiter.api.response.SwapJupiterV6QuoteResponse

interface SwapJupiterV6Api {
    @POST("v6/swap")
    suspend fun createRouteSwapTransaction(
        @Body body: CreateSwapTransactionV6Request
    ): CreateSwapTransactionV6Response

    @GET("v6/quote")
    suspend fun getSwapRoute(
        @Query("inputMint") inputMint: String,
        @Query("outputMint") outputMint: String,
        @Query("amount") amountInLamports: BigInteger,
        @Query("slippageBps") slippageBps: Int,
        // added by our own backend
        @Query("userPublicKey") userPublicKey: String
    ): SwapJupiterV6QuoteResponse
}
