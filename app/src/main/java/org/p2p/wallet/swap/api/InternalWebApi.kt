package org.p2p.wallet.swap.api

import retrofit2.http.GET
import retrofit2.http.Path

interface InternalWebApi {

    @GET("tokens/{network}.json")
    suspend fun loadTokens(@Path("network") network: String): Map<String, OrcaTokensResponse>

    @GET("aquafarms/{network}.json")
    suspend fun loadAquafarms(@Path("network") network: String): Map<String, OrcaAquafarmResponse>

    @GET("pools/{network}.json")
    suspend fun loadPools(@Path("network") network: String): Map<String, OrcaPoolResponse>

    @GET("programIds/{network}.json")
    suspend fun loadProgramId(@Path("network") network: String): ProgramIdResponse
}
