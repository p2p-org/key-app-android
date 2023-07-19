package org.p2p.wallet.user.api

import retrofit2.http.GET
import org.p2p.token.service.api.response.TokenListResponse

interface SolanaApi {

    @GET("solana.tokenlist.json")
    suspend fun loadTokenlist(): TokenListResponse
}
