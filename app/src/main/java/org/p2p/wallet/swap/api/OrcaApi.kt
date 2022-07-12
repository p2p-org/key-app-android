package org.p2p.wallet.swap.api

import retrofit2.http.GET

interface OrcaApi {

    @GET("info")
    suspend fun loadConfigs(): OrcaInfoResponse
}
