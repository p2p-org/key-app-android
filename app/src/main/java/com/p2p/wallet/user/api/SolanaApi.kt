package com.p2p.wallet.user.api

import com.p2p.wallet.user.local.TokenListResponse
import retrofit2.http.GET

interface SolanaApi {

    @GET("solana.tokenlist.json")
    suspend fun loadTokenlist(): TokenListResponse
}