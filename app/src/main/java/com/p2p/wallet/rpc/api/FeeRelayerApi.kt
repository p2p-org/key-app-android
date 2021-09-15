package com.p2p.wallet.rpc.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FeeRelayerApi {

    @GET("fee_payer/pubkey")
    suspend fun getPublicKey(): String

    @POST("transfer_sol")
    suspend fun sendSolToken(@Body request: FeeSolTransferRequest): List<String>

    @POST("transfer_spl_token")
    suspend fun sendSplToken(@Body request: FeeSplTransferRequest): List<String>
}