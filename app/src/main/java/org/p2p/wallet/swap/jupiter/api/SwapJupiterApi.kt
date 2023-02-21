package org.p2p.wallet.swap.jupiter.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url
import java.math.BigInteger
import org.p2p.wallet.swap.jupiter.api.request.CreateSwapTransactionRequest
import org.p2p.wallet.swap.jupiter.api.response.CreateSwapTransactionResponse
import org.p2p.wallet.swap.jupiter.api.response.JupiterAllSwapRoutesResponse
import org.p2p.wallet.swap.jupiter.api.response.SwapJupiterQuoteResponse
import org.p2p.wallet.swap.jupiter.api.response.tokens.JupiterTokenResponse
import org.p2p.wallet.utils.Base58String

interface SwapJupiterApi {
    @GET
    suspend fun getSwapTokens(
        @Url url: String = "https://cache.jup.ag//tokens"
    ): List<JupiterTokenResponse>

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
        @Query("asLegacyTransaction") asLegacyTransaction: Boolean = true
    ): SwapJupiterQuoteResponse

    @GET("v4/indexed-route-map")
    suspend fun getSwapRoutesMap(): JupiterAllSwapRoutesResponse
}
