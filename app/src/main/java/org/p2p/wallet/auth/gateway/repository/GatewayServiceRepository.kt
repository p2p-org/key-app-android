package org.p2p.wallet.auth.gateway.repository

import com.google.gson.JsonObject
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceStandardResponse
import org.p2p.wallet.auth.gateway.api.response.RegisterWalletResponse
import org.p2p.wallet.auth.model.Web3AuthSignUpResponse
import org.p2p.wallet.utils.Base58String

interface GatewayServiceRepository {
    suspend fun registerWalletWithSms(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        etheriumPublicKey: String,
        phoneNumber: String
    ): RegisterWalletResponse

    suspend fun confirmRegisterWallet(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        etheriumPublicKey: String,
        thirdShare: Web3AuthSignUpResponse.ShareRootDetails.ShareInnerDetails.ShareValue,
        jsonEncryptedMnemonicPhrase: JsonObject,
        phoneNumber: String,
        otpConfirmationCode: String
    ): GatewayServiceStandardResponse
}
