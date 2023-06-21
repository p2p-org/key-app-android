package org.p2p.wallet.auth.interactor

import org.p2p.core.crashlytics.CrashLogger
import org.p2p.wallet.auth.gateway.repository.GatewayServiceRepository
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.repository.SignUpFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.smsinput.SmsInputTimer
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseSource
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class CreateWalletInteractor(
    private val gatewayServiceRepository: GatewayServiceRepository,
    private val signUpFlowDataRepository: SignUpFlowDataLocalRepository,
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
    private val smsInputTimer: SmsInputTimer,
    private val tokenKeyProvider: TokenKeyProvider,
    private val seedPhraseProvider: SeedPhraseProvider,
    private val crashLogger: CrashLogger
) {
    class CreateWalletFailure(override val message: String) : Throwable(message)

    val timer
        get() = smsInputTimer.smsInputTimerFlow

    suspend fun startCreatingWallet(userPhoneNumber: PhoneNumber, isResend: Boolean = false) {
        val userPublicKey = signUpFlowDataRepository.userPublicKey
            ?: throw CreateWalletFailure("User public key is null")
        val userPrivateKey = signUpFlowDataRepository.userPrivateKeyB58
            ?: throw CreateWalletFailure("User private key is null")
        val etheriumPublicKey = signUpFlowDataRepository.ethereumPublicKey
            ?: throw CreateWalletFailure("User etherium public key is null")

        val isNumberAlreadyUsed = userPhoneNumber == signUpFlowDataRepository.userPhoneNumber
        setIsCreateWalletRequestSent(isSent = isNumberAlreadyUsed)
        val isCreateWalletRequestSent = signUpFlowDataRepository.isCreateWalletRequestSent
        val isCreateRequestNeeded = isResend || (!isCreateWalletRequestSent && !isNumberAlreadyUsed)
        if (isCreateRequestNeeded) {
            gatewayServiceRepository.registerWalletWithSms(
                userPublicKey = userPublicKey,
                userPrivateKey = userPrivateKey,
                etheriumAddress = etheriumPublicKey,
                phoneNumber = userPhoneNumber
            )
            signUpFlowDataRepository.userPhoneNumber = userPhoneNumber
            setIsCreateWalletRequestSent(isSent = true)
            if (!isResend) {
                smsInputTimer.resetSmsCount()
            }
            smsInputTimer.startSmsInputTimerFlow()
        }
    }

    fun getUserEnterPhoneNumberTriesCount() = signUpFlowDataRepository.userPhoneNumberEnteredCount

    fun resetUserEnterPhoneNumberTriesCount() {
        signUpFlowDataRepository.resetUserPhoneNumberEnteredCount()
    }

    fun getUserPhoneNumber() = signUpFlowDataRepository.userPhoneNumber

    suspend fun finishCreatingWallet(smsCode: String) {
        require(smsCode.length == 6) { "SMS code is not valid" }

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

        val userSeedPhrase: List<String>
        val socialShareOwnerId: String
        userSignUpDetailsStorage.getLastSignUpUserDetails().also {
            userSeedPhrase = it?.signUpDetails?.mnemonicPhraseWords
                ?: throw CreateWalletFailure("Mnemonic phrase is null")
            socialShareOwnerId = it.userId
        }

        gatewayServiceRepository.confirmRegisterWallet(
            userPublicKey = userPublicKey,
            userPrivateKey = userPrivateKey,
            etheriumAddress = etheriumPublicKey,
            thirdShare = thirdShare,
            phoneNumber = phoneNumber,
            jsonEncryptedMnemonicPhrase = encryptedMnemonicPhrase,
            otpConfirmationCode = smsCode,
            userSeedPhrase = userSeedPhrase,
            socialShareOwnerId = socialShareOwnerId
        )

        seedPhraseProvider.setUserSeedPhrase(
            words = userSeedPhrase,
            provider = SeedPhraseSource.WEB_AUTH
        )

        finishAuthFlow()
    }

    private fun finishAuthFlow() {
        signUpFlowDataRepository.userAccount?.also {
            tokenKeyProvider.keyPair = it.keypair
            tokenKeyProvider.publicKey = it.publicKey.toBase58()
            crashLogger.setUserId(tokenKeyProvider.publicKey)
        } ?: throw CreateWalletFailure("User account is null, creating a user is failed")

        signUpFlowDataRepository.clear()
    }

    fun setIsCreateWalletRequestSent(isSent: Boolean) {
        signUpFlowDataRepository.isCreateWalletRequestSent = isSent
    }
}
