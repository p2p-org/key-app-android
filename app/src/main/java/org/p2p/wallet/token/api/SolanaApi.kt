package org.p2p.wallet.token.api

import org.p2p.wallet.token.api.model.TokenListResponse
import retrofit2.http.GET

interface SolanaApi {

    @GET("solana.tokenlist.json")
    suspend fun loadTokenlist(): TokenListResponse
}
