package org.p2p.wallet.swap.jupiter.api

import org.p2p.solanaj.utils.crypto.Base64String
import org.p2p.wallet.swap.jupiter.api.request.CreateSwapTransactionRequest
import org.p2p.wallet.swap.jupiter.api.response.CreateSwapTransactionResponse
import org.p2p.wallet.swap.jupiter.api.response.quote.SwapJupiterQuoteResponse
import org.p2p.wallet.utils.Base58String
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.math.BigDecimal

interface SwapJupiterApi {
    @POST("v4/swap")
    suspend fun createRouteSwapTransaction(
        @Body body: CreateSwapTransactionRequest
    ): CreateSwapTransactionResponse

    @GET("v4/quote")
    suspend fun getQuote(
        @Query("inputMint") inputMint: Base64String,
        @Query("outputMint") outputMint: Base64String,
        @Query("amount") amount: BigDecimal,
        @Query("userPublicKey") userPublicKey: Base58String,
    ): SwapJupiterQuoteResponse
}
