package org.p2p.wallet.rpc.api

import com.google.gson.JsonArray
import org.json.JSONArray
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FeeRelayerApi {

    @GET("fee_payer/pubkey")
    suspend fun getPublicKey(): String

    @GET("v2/fee_payer/pubkey")
    suspend fun getPublicKeyV2(): String

    @POST("relay_transaction")
    suspend fun send(@Body request: SendTransactionRequest): JsonArray

    @POST("v2/relay_transaction")
    suspend fun sendV2(@Body request: SendTransactionRequest): JsonArray

    @POST("transfer_sol")
    suspend fun sendSolToken(@Body request: FeeSolTransferRequest): List<String>

    @POST("transfer_spl_token")
    suspend fun sendSplToken(@Body request: FeeSplTransferRequest): List<String>
}