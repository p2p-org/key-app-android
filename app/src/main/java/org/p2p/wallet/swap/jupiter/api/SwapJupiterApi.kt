package org.p2p.wallet.swap.jupiter.api

import org.p2p.wallet.swap.jupiter.api.request.CreateSwapTransactionRequest
import org.p2p.wallet.swap.jupiter.api.response.CreateSwapTransactionResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface SwapJupiterApi {
    @POST("v4/swap")
    suspend fun createRouteSwapTransaction(
        @Body body: CreateSwapTransactionRequest
    ): CreateSwapTransactionResponse
}
