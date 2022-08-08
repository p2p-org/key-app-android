package org.p2p.wallet.auth.interactor

import org.p2p.wallet.auth.gateway.repository.GatewayServiceRepository
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class CreateWalletInteractor(
    private val gatewayServiceRepository: GatewayServiceRepository,
    private val signUpFlowDataCache: SignUpFlowDataCache,
    private val tokenKeyProvider: TokenKeyProvider,
) {
    class CreateWalletFailure(override val message: String) : Throwable(message)

    suspend fun startCreatingWallet(userPhoneNumber: String) {
        val userPublicKey = signUpFlowDataCache.userPublicKey
            ?: throw CreateWalletFailure("User public key is null")
        val userPrivateKey = signUpFlowDataCache.userPrivateKeyB58
            ?: throw CreateWalletFailure("User private key is null")
        val etheriumPublicKey = signUpFlowDataCache.ethereumPublicKey
            ?: throw CreateWalletFailure("User etherium public key is null")

        gatewayServiceRepository.registerWalletWithSms(
            userPublicKey = userPublicKey,
            userPrivateKey = userPrivateKey,
            etheriumPublicKey = etheriumPublicKey,
            phoneNumber = userPhoneNumber
        )
        signUpFlowDataCache.userPhoneNumber = userPhoneNumber
    }

    suspend fun finishCreatingWallet(smsCode: String) {
        val userPublicKey = signUpFlowDataCache.userPublicKey
            ?: throw CreateWalletFailure("User public key is null")
        val userPrivateKey = signUpFlowDataCache.userPrivateKeyB58
            ?: throw CreateWalletFailure("User private key is null")
        val etheriumPublicKey = signUpFlowDataCache.ethereumPublicKey
            ?: throw CreateWalletFailure("User etherium public key is null")
        val thirdShare = signUpFlowDataCache.thirdShare
            ?: throw CreateWalletFailure("Custom third share is null")
        val encryptedMnemonicPhrase = signUpFlowDataCache.encryptedMnemonicPhrase
            ?: throw CreateWalletFailure("Encrypted mnemonic phrase is null")
        val phoneNumber = signUpFlowDataCache.userPhoneNumber
            ?: throw CreateWalletFailure("User phone number is null")

        gatewayServiceRepository.confirmRegisterWallet(
            userPublicKey = userPublicKey,
            userPrivateKey = userPrivateKey,
            etheriumPublicKey = etheriumPublicKey,
            thirdShare = thirdShare,
            phoneNumber = phoneNumber,
            jsonEncryptedMnemonicPhrase = encryptedMnemonicPhrase,
            otpConfirmationCode = smsCode
        )
    }

    fun finishAuthFlow() {
        signUpFlowDataCache.userAccount?.also {
            tokenKeyProvider.secretKey = it.secretKey
            tokenKeyProvider.publicKey = it.publicKey.toBase58()
        } ?: throw CreateWalletFailure("User account is null")
    }
}
