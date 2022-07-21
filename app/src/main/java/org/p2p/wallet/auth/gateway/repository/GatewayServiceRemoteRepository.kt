package org.p2p.wallet.auth.gateway.repository

import org.p2p.wallet.auth.gateway.api.GatewayServiceApi
import org.p2p.wallet.auth.gateway.api.request.RegisterWalletRequest
import org.p2p.wallet.auth.gateway.api.response.RegisterWalletResponse
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import kotlinx.coroutines.withContext

class GatewayServiceRemoteRepository(
    private val api: GatewayServiceApi,
    private val mapper: GatewayServiceMapper,
    private val dispatchers: CoroutineDispatchers
) : GatewayServiceRepository {

    override suspend fun registerWalletWithSms(
        userPublicKey: String,
        userPrivateKey: String,
        etheriumPublicKey: String,
        phoneNumber: String
    ): RegisterWalletResponse = withContext(dispatchers.io) {
        val request = mapper.toNetwork(
            userPublicKey = userPublicKey,
            userPrivateKey = userPrivateKey,
            etheriumPublicKey = etheriumPublicKey,
            phoneNumber = phoneNumber,
            channel = RegisterWalletRequest.OtpMethod.SMS
        )
        val response = api.registerWallet(request)
        mapper.fromNetwork(response)
    }
}
