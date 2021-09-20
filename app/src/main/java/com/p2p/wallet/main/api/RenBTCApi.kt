package com.p2p.wallet.main.api

import retrofit2.http.GET
import retrofit2.http.Path

interface RenBTCApi {

    @GET("{network}/api/address/{gateway}/utxo")
    suspend fun getPaymentData(
        @Path("network") network: String,
        @Path("gateway") gateway: String
    ): List<RenBTCPaymentResponse>

    @GET("api/address/{gateway}/utxo")
    suspend fun getPaymentData(@Path("gateway") gateway: String): List<RenBTCPaymentResponse>
}