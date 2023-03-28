package org.p2p.wallet.feerelayer.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FeeRelayerDevnetApi {

    @GET("v2/fee_payer/pubkey")
    suspend fun getPublicKeyV2(): String

    @GET("v2/free_fee_limits/{owner}")
    suspend fun getFreeFeeLimits(@Path("owner") owner: String): FreeFeeLimitsResponse

    @POST("v2/relay_transaction")
    suspend fun relayTransactionV2(@Body request: SendTransactionRequest): List<String>

    @POST("v2/sign_relay_transaction")
    suspend fun signTransactionV2(@Body request: SignTransactionRequest): SignTransactionResponse

    @POST("v2/relay_top_up_with_swap")
    suspend fun relayTopUpSwapV2(@Body request: RelayTopUpSwapRequest): List<String>
}
