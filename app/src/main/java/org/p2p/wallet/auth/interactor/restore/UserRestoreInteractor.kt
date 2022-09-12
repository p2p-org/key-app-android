package org.p2p.wallet.auth.interactor.restore

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.p2p.solanaj.utils.crypto.decodeFromBase64
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.RestoreUserResult
import org.p2p.wallet.auth.model.RestoreWalletFailure
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignInResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.utils.fromJsonReified
import timber.log.Timber

class UserRestoreInteractor(
    private val web3AuthApi: Web3AuthApi,
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository,
    private val signUpDetailsStorage: UserSignUpDetailsStorage,
    private val tokenKeyProvider: TokenKeyProvider,
    private val gson: Gson
) {

    fun isUserReadyToBeRestored(restoreFlow: OnboardingFlow.RestoreWallet): Boolean {
        return when (restoreFlow) {
            is OnboardingFlow.RestoreWallet.SocialPlusCustomShare -> {
                restoreFlowDataLocalRepository.socialShare != null && restoreFlowDataLocalRepository.customShare != null
            }
            is OnboardingFlow.RestoreWallet.DevicePlusCustomShare -> {
                restoreFlowDataLocalRepository.deviceShare != null && restoreFlowDataLocalRepository.customShare != null
            }
            else -> {
                false
            }
        }
    }

    suspend fun tryRestoreUser(restoreFlow: OnboardingFlow.RestoreWallet): RestoreUserResult = try {
        when (restoreFlow) {
            is OnboardingFlow.RestoreWallet.SocialPlusCustomShare -> {
                tryRestoreUser(restoreFlow)
            }
            is OnboardingFlow.RestoreWallet.DevicePlusCustomShare -> {
                tryRestoreUser(restoreFlow)
            }
            else -> {
                throw IllegalStateException("Unknown restore flow")
            }
        }
    } catch (error: Throwable) {
        RestoreUserResult.RestoreFailed(error)
    }

    private suspend fun tryRestoreUser(
        restoreWay: OnboardingFlow.RestoreWallet.SocialPlusCustomShare
    ): RestoreUserResult = try {
        val customShare = restoreFlowDataLocalRepository.customShare
            ?: throw IllegalStateException("Social+Custom restore way failed. Third share is null")
        val socialShare = restoreFlowDataLocalRepository.socialShare
            ?: throw IllegalStateException("Social+Custom restore way failed. Social share is null")
        val socialShareUserId = restoreFlowDataLocalRepository.socialShareUserId
            ?: throw IllegalStateException("Social+Custom restore way failed. Social share ID is null")
        val result: Web3AuthSignInResponse = web3AuthApi.triggerSignInNoDevice(socialShare, customShare)

        signUpDetailsStorage.save(
            data = Web3AuthSignUpResponse(
                ethereumPublicKey = result.ethereumPublicKey,
                mnemonicPhrase = result.mnemonicPhrase,
                encryptedMnemonicPhrase = JsonObject(),
                deviceShare = null,
                customThirdShare = customShare
            ),
            userId = socialShareUserId
        )

        restoreFlowDataLocalRepository.generateActualAccount(result.mnemonicPhrase.split(""))
        RestoreUserResult.RestoreSuccessful
    } catch (error: Throwable) {
        RestoreUserResult.RestoreFailed(error)
    }

    private suspend fun tryRestoreUser(
        restoreFlow: OnboardingFlow.RestoreWallet.DevicePlusCustomShare
    ): RestoreUserResult = try {
        val customShare = restoreFlowDataLocalRepository.customShare
            ?: throw IllegalStateException("Device+Custom restore way failed. Third share is null")
        val deviceShare = restoreFlowDataLocalRepository.deviceShare
            ?: throw IllegalStateException("Device+Custom restore way failed. Device share is null")
        val encryptedMnemonicGson = restoreFlowDataLocalRepository.encryptedMnemonic?.let {
            convertBase64ToEncryptedMnemonics(it)
        } ?: throw IllegalStateException("Device+Custom restore way failed. Mnemonic phrase is null")

        val result: Web3AuthSignInResponse = web3AuthApi.triggerSignInNoTorus(
            deviceShare = deviceShare,
            thirdShare = customShare,
            encryptedMnemonicPhrase = encryptedMnemonicGson
        )
        restoreFlowDataLocalRepository.generateActualAccount(result.mnemonicPhrase.split(""))
        RestoreUserResult.RestoreSuccessful
    } catch (e: Throwable) {
        RestoreUserResult.RestoreFailed(e)
    }

    fun finishAuthFlow() {
        restoreFlowDataLocalRepository.userActualAccount?.also {
            tokenKeyProvider.secretKey = it.secretKey
            tokenKeyProvider.publicKey = it.publicKey.toBase58()
        } ?: throw NullPointerException("User actual account is null, restoring a user is failed")
    }

    private fun convertBase64ToEncryptedMnemonics(
        encryptedMnemonicsStruct: String
    ): JsonObject {
        val encryptedMnemonicsJson = String(encryptedMnemonicsStruct.decodeFromBase64())
        return gson.fromJsonReified<JsonObject>(encryptedMnemonicsJson)
            ?: run {
                Timber.i(encryptedMnemonicsStruct)
                Timber.i(encryptedMnemonicsJson)
                throw RestoreWalletFailure("Couldn't convert base64 to encrypted mnemonics")
            }
    }
}
