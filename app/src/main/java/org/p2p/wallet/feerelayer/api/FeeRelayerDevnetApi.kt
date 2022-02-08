package org.p2p.wallet.feerelayer.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FeeRelayerDevnetApi {

    @GET("v2/fee_payer/pubkey")
    suspend fun getPublicKeyV2(): String

    @POST("v2/relay_transaction")
    suspend fun relayTransactionV2(@Body request: SendTransactionRequest): List<String>

    @POST("v2/relay_top_up_with_swap")
    suspend fun relayTopUpSwapV2(@Body request: RelayTopUpSwapRequest): List<String>

    @POST("v2/relay_transfer_spl_token")
    suspend fun relayTransferSplTokenV2(@Body request: RelayTransferRequest): List<String>
}