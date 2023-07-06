package org.p2p.wallet.auth.gateway.repository

import com.google.gson.JsonObject
import org.p2p.wallet.auth.gateway.api.request.OtpMethod
import org.p2p.wallet.auth.gateway.api.response.ConfirmRestoreWalletResponse
import org.p2p.wallet.auth.gateway.api.response.GatewayServiceStandardResponse
import org.p2p.wallet.auth.gateway.api.response.RegisterWalletResponse
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.core.crypto.Base58String

interface GatewayServiceRepository {
    suspend fun registerWalletWithSms(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        etheriumAddress: String,
        phoneNumber: PhoneNumber
    ): RegisterWalletResponse

    suspend fun confirmRegisterWallet(
        userPublicKey: Base58String,
        userPrivateKey: Base58String,
        etheriumAddress: String,
        thirdShare: Web3AuthSignUpResponse.ShareDetailsWithMeta,
        jsonEncryptedMnemonicPhrase: JsonObject,
        phoneNumber: PhoneNumber,
        userSeedPhrase: List<String>,
        socialShareOwnerId: String,
        otpConfirmationCode: String
    ): GatewayServiceStandardResponse

    suspend fun restoreWallet(
        solanaPublicKey: Base58String,
        solanaPrivateKey: Base58String,
        phoneNumber: PhoneNumber,
        channel: OtpMethod
    ): GatewayServiceStandardResponse

    suspend fun confirmRestoreWallet(
        solanaPublicKey: Base58String,
        solanaPrivateKey: Base58String,
        phoneNumber: PhoneNumber,
        otpConfirmationCode: String
    ): ConfirmRestoreWalletResponse

    suspend fun loadAndSaveOnboardingMetadata(
        solanaPublicKey: Base58String,
        solanaPrivateKey: Base58String,
        userSeedPhrase: List<String>,
        etheriumAddress: String
    )
}
