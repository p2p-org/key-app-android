package org.p2p.wallet.swap.jupiter.api

import org.p2p.wallet.swap.jupiter.api.response.tokens.JupiterTokenResponse
import retrofit2.http.GET

interface SwapJupiterTokensApi {
    @GET("tokens")
    suspend fun getTokens(): List<JupiterTokenResponse>
}
