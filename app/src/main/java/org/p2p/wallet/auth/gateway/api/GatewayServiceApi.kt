package org.p2p.wallet.auth.gateway.api

import org.p2p.wallet.auth.gateway.api.request.ConfirmRegisterWalletRequest
import org.p2p.wallet.auth.gateway.api.request.GatewayServiceRequest
import org.p2p.wallet.auth.gateway.api.request.RegisterWalletRequest
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceResponse
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceStandardResponse
import org.p2p.wallet.auth.gateway.api.response.RegisterWalletResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface GatewayServiceApi {
    @POST
    suspend fun registerWallet(@Body request: GatewayServiceRequest<RegisterWalletRequest>):
        GatewayServiceResponse<RegisterWalletResponse>

    @POST
    suspend fun confirmRegisterWallet(@Body request: GatewayServiceRequest<ConfirmRegisterWalletRequest>):
        GatewayServiceResponse<GatewayServiceStandardResponse>
}
