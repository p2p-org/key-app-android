package org.p2p.wallet.auth.gateway.api

import org.p2p.wallet.auth.gateway.api.request.RegisterWalletRequest
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceResponse
import org.p2p.wallet.auth.gateway.api.response.RegisterWalletResponse
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GatewayServiceApi {
    @Headers(
        "Content-Type: application/json",
        "Accept: application/json",
    )
    @POST
    suspend fun registerWallet(@Body request: RegisterWalletRequest): GatewayServiceResponse<RegisterWalletResponse>
}
