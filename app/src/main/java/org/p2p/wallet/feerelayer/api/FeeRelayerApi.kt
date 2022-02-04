package org.p2p.wallet.feerelayer.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FeeRelayerApi {

    @GET("fee_payer/pubkey")
    suspend fun getPublicKey(): String

    @POST("relay_transaction")
    suspend fun relayTransaction(@Body request: SendTransactionRequest): List<String>

    @POST("relay_top_up_with_swap")
    suspend fun relayTopUpSwap(@Body request: RelayTopUpSwapRequest): List<String>

    @POST("relay_swap")
    suspend fun relaySwap(@Body request: RelaySwapRequest): List<String>

    @POST("relay_transfer_spl_token")
    suspend fun relayTransferSplToken(@Body request: RelayTransferRequest): List<String>
}