package org.p2p.wallet.auth.gateway.repository

import com.google.gson.JsonObject
import org.p2p.wallet.auth.gateway.api.response.ConfirmRestoreWalletResponse
import org.p2p.wallet.auth.gateway.api.request.OtpMethod
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceStandardResponse
import org.p2p.wallet.auth.gateway.api.response.RegisterWalletResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.utils.Base58String

interface GatewayServiceRepository {
    suspend fun registerWalletWithSms(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        etheriumPublicKey: String,
        e164PhoneNumber: String
    ): RegisterWalletResponse

    suspend fun confirmRegisterWallet(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        etheriumPublicKey: String,
        thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta.ShareInnerDetails.ShareValue,
        jsonEncryptedMnemonicPhrase: JsonObject,
        phoneNumber: String,
        otpConfirmationCode: String
    ): GatewayServiceStandardResponse

    suspend fun restoreWallet(
        solanaPublicKey: Base58String,
        solanaPrivateKey: Base58String,
        phoneNumber: String,
        channel: OtpMethod
    ): GatewayServiceStandardResponse

    suspend fun confirmRestoreWallet(
        solanaPublicKey: Base58String,
        solanaPrivateKey: Base58String,
        phoneNumber: String,
        otpConfirmationCode: String
    ): ConfirmRestoreWalletResponse
}
