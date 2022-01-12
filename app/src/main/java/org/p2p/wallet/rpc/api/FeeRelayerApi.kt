package org.p2p.wallet.rpc.api

import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FeeRelayerApi {

    @GET("v2/fee_payer/pubkey")
    suspend fun getPublicKey(): String

    @POST("v2/relay_transaction")
    suspend fun send(@Body request: SendTransactionRequest): JsonObject

    @POST("transfer_sol")
    suspend fun sendSolToken(@Body request: FeeSolTransferRequest): List<String>

    @POST("transfer_spl_token")
    suspend fun sendSplToken(@Body request: FeeSplTransferRequest): List<String>
}