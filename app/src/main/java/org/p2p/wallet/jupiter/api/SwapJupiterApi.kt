package org.p2p.wallet.jupiter.api

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming
import java.math.BigInteger
import org.p2p.wallet.jupiter.api.request.CreateSwapTransactionRequest
import org.p2p.wallet.jupiter.api.response.CreateSwapTransactionResponse
import org.p2p.wallet.jupiter.api.response.SwapJupiterQuoteResponse
import org.p2p.wallet.jupiter.api.response.tokens.JupiterTokenResponse

interface SwapJupiterApi {
    @GET("v4/tokens")
    suspend fun getSwapTokens(): List<JupiterTokenResponse>

    @POST("v4/swap")
    suspend fun createRouteSwapTransaction(
        @Body body: CreateSwapTransactionRequest
    ): CreateSwapTransactionResponse

    @GET("v4/quote")
    suspend fun getSwapRoutes(
        @Query("inputMint") inputMint: String,
        @Query("outputMint") outputMint: String,
        @Query("amount") amountInLamports: BigInteger,
        @Query("userPublicKey") userPublicKey: String,
        @Query("slippageBps") slippageBps: Int
    ): SwapJupiterQuoteResponse

    @Streaming
    @GET("v4/indexed-route-map")
    suspend fun getSwapRoutesMap(): ResponseBody
}
