package org.p2p.wallet.auth.interactor

import org.p2p.wallet.auth.gateway.repository.GatewayServiceRepository
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.repository.SignUpFlowDataLocalRepository
import org.p2p.wallet.auth.ui.smsinput.SmsInputTimer
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class CreateWalletInteractor(
    private val gatewayServiceRepository: GatewayServiceRepository,
    private val signUpFlowDataRepository: SignUpFlowDataLocalRepository,
    private val smsInputTimer: SmsInputTimer,
    private val tokenKeyProvider: TokenKeyProvider
) {
    class CreateWalletFailure(override val message: String) : Throwable(message)

    val timer
        get() = smsInputTimer.smsInputTimerFlow

    val resetCount
        get() = smsInputTimer.smsResendCount

    suspend fun startCreatingWallet(userPhoneNumber: PhoneNumber, isResend: Boolean = false) {
        val userPublicKey = signUpFlowDataRepository.userPublicKey
            ?: throw CreateWalletFailure("User public key is null")
        val userPrivateKey = signUpFlowDataRepository.userPrivateKeyB58
            ?: throw CreateWalletFailure("User private key is null")
        val etheriumPublicKey = signUpFlowDataRepository.ethereumPublicKey
            ?: throw CreateWalletFailure("User etherium public key is null")

        if (isResend || userPhoneNumber != signUpFlowDataRepository.userPhoneNumber) {
            gatewayServiceRepository.registerWalletWithSms(
                userPublicKey = userPublicKey,
                userPrivateKey = userPrivateKey,
                etheriumAddress = etheriumPublicKey,
                phoneNumber = userPhoneNumber
            )
            smsInputTimer.startSmsInputTimerFlow()
        }
        signUpFlowDataRepository.userPhoneNumber = userPhoneNumber
    }

    fun getUserEnterPhoneNumberTriesCount() = signUpFlowDataRepository.userPhoneNumberEnteredCount

    fun getUserPhoneNumber() = signUpFlowDataRepository.userPhoneNumber

    suspend fun finishCreatingWallet(smsCode: String) {
        val userPublicKey = signUpFlowDataRepository.userPublicKey
            ?: throw CreateWalletFailure("User public key is null")
        val userPrivateKey = signUpFlowDataRepository.userPrivateKeyB58
            ?: throw CreateWalletFailure("User private key is null")
        val etheriumPublicKey = signUpFlowDataRepository.ethereumPublicKey
            ?: throw CreateWalletFailure("User etherium public key is null")
        val thirdShare = signUpFlowDataRepository.thirdShare
            ?: throw CreateWalletFailure("Custom third share is null")
        val encryptedMnemonicPhrase = signUpFlowDataRepository.encryptedMnemonicPhrase
            ?: throw CreateWalletFailure("Encrypted mnemonic phrase is null")
        val phoneNumber = signUpFlowDataRepository.userPhoneNumber
            ?: throw CreateWalletFailure("User phone number is null")

        gatewayServiceRepository.confirmRegisterWallet(
            userPublicKey = userPublicKey,
            userPrivateKey = userPrivateKey,
            etheriumAddress = etheriumPublicKey,
            thirdShare = thirdShare,
            phoneNumber = phoneNumber,
            jsonEncryptedMnemonicPhrase = encryptedMnemonicPhrase,
            otpConfirmationCode = smsCode
        )
    }

    fun finishAuthFlow() {
        signUpFlowDataRepository.userAccount?.also {
            tokenKeyProvider.secretKey = it.secretKey
            tokenKeyProvider.publicKey = it.publicKey.toBase58()
        } ?: throw CreateWalletFailure("User account is null, creating a user is failed")

        signUpFlowDataRepository.clear()
    }
}
