package org.p2p.wallet.auth.gateway.repository

import com.google.gson.JsonObject
import kotlinx.coroutines.withContext
import org.p2p.wallet.auth.gateway.api.GatewayServiceApi
import org.p2p.wallet.auth.gateway.api.request.OtpMethod
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceStandardResponse
import org.p2p.wallet.auth.gateway.api.response.RegisterWalletResponse
import org.p2p.wallet.auth.model.Web3AuthSignUpResponse
import org.p2p.wallet.infrastructure.dispatchers.CoroutineDispatchers
import org.p2p.wallet.utils.Base58String

class GatewayServiceRemoteRepository(
    private val api: GatewayServiceApi,
    private val createWalletMapper: GatewayServiceCreateWalletMapper,
    private val dispatchers: CoroutineDispatchers
) : GatewayServiceRepository {

    override suspend fun registerWalletWithSms(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        etheriumPublicKey: String,
        phoneNumber: String
    ): RegisterWalletResponse = withContext(dispatchers.io) {
        val request = createWalletMapper.toRegisterWalletNetwork(
            userPublicKey = userPublicKey,
            userPrivateKey = userPrivateKey,
            etheriumPublicKey = etheriumPublicKey,
            phoneNumber = phoneNumber,
            channel = OtpMethod.SMS
        )
        val response = api.registerWallet(request)
        createWalletMapper.fromNetwork(response)
    }

    override suspend fun confirmRegisterWallet(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        etheriumPublicKey: String,
        thirdShare: Web3AuthSignUpResponse.ShareRootDetails.ShareInnerDetails.ShareValue,
        jsonEncryptedMnemonicPhrase: JsonObject,
        phoneNumber: String,
        otpConfirmationCode: String
    ): GatewayServiceStandardResponse = withContext(dispatchers.io) {
        val request = createWalletMapper.toConfirmRegisterWalletNetwork(
            userPublicKey = userPublicKey,
            userPrivateKey = userPrivateKey,
            etheriumPublicKey = etheriumPublicKey,
            thirdShare = thirdShare,
            jsonEncryptedMnemonicPhrase = jsonEncryptedMnemonicPhrase,
            phoneNumber = phoneNumber,
            otpConfirmationCode = otpConfirmationCode
        )
        val response = api.confirmRegisterWallet(request)
        createWalletMapper.fromNetwork(response)
    }
}
