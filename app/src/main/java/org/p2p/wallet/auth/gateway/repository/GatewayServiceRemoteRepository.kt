package org.p2p.wallet.auth.gateway.repository

import org.p2p.wallet.auth.gateway.api.GatewayServiceApi
import org.p2p.wallet.auth.gateway.api.request.OtpMethod
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
        val request = mapper.toRegisterWalletNetwork(
            userPublicKey = userPublicKey,
            userPrivateKey = userPrivateKey,
            etheriumPublicKey = etheriumPublicKey,
            phoneNumber = phoneNumber,
            channel = OtpMethod.SMS
        )
        val response = api.registerWallet(request)
        mapper.fromNetwork(response)
    }

    override suspend fun confirmRegisterWallet(
        userPublicKey: String,
        userPrivateKey: String,
        etheriumPublicKey: String,
        thirdShare: String,
        phoneNumber: String,
        otpConfirmationCode: String
    ) {
        val request = mapper.toRegisterWalletNetwork(
            userPublicKey = userPublicKey,
            userPrivateKey = userPrivateKey,
            etheriumPublicKey = etheriumPublicKey,
            phoneNumber = phoneNumber,
            channel = OtpMethod.SMS
        )
        val response = api.registerWallet(request)
        mapper.fromNetwork(response)
    }
}
