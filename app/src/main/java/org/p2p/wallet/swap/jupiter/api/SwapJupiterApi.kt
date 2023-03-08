package org.p2p.wallet.swap.jupiter.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.math.BigInteger
import org.p2p.wallet.swap.jupiter.api.request.CreateSwapTransactionRequest
import org.p2p.wallet.swap.jupiter.api.response.CreateSwapTransactionResponse
import org.p2p.wallet.swap.jupiter.api.response.JupiterAllSwapRoutesResponse
import org.p2p.wallet.swap.jupiter.api.response.SwapJupiterQuoteResponse
import org.p2p.wallet.swap.jupiter.api.response.tokens.JupiterTokenResponse

interface SwapJupiterApi {
    @GET("tokens")
    suspend fun getSwapTokens(): List<JupiterTokenResponse>

    @POST("swap")
    suspend fun createRouteSwapTransaction(
        @Body body: CreateSwapTransactionRequest
    ): CreateSwapTransactionResponse

    @GET("quote")
    suspend fun getSwapRoutes(
        @Query("inputMint") inputMint: String,
        @Query("outputMint") outputMint: String,
        @Query("amount") amountInLamports: BigInteger,
        @Query("userPublicKey") userPublicKey: String,
        @Query("slippageBps") slippageBps: Int
    ): SwapJupiterQuoteResponse

    @GET("indexed-route-map")
    suspend fun getSwapRoutesMap(): JupiterAllSwapRoutesResponse
}
