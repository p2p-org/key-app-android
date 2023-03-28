package org.p2p.wallet.feerelayer.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FeeRelayerApi {

    @GET("fee_payer/pubkey")
    suspend fun getPublicKey(): String

    @GET("free_fee_limits/{owner}")
    suspend fun getFreeFeeLimits(@Path("owner") owner: String): FreeFeeLimitsResponse

    @POST("relay_transaction")
    suspend fun relayTransaction(@Body request: SendTransactionRequest): List<String>

    @POST("sign_relay_transaction")
    suspend fun signRelayTransaction(@Body request: SignTransactionRequest): List<String>

    @POST("relay_top_up_with_swap")
    suspend fun relayTopUpSwap(@Body request: RelayTopUpSwapRequest): List<String>
}
