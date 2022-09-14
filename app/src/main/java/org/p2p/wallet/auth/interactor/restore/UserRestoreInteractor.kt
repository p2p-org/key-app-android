package org.p2p.wallet.auth.interactor.restore

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.RestoreUserResult
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignInResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.utils.fromJsonReified

class UserRestoreInteractor(
    private val web3AuthApi: Web3AuthApi,
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository,
    private val signUpDetailsStorage: UserSignUpDetailsStorage,
    private val tokenKeyProvider: TokenKeyProvider,
    private val gson: Gson
) {

    fun isUserReadyToBeRestored(restoreFlow: OnboardingFlow.RestoreWallet): Boolean {
        val sharesCount = when (restoreFlow) {
            is OnboardingFlow.RestoreWallet.SocialPlusCustomShare -> {
                countShare(restoreFlowDataLocalRepository.socialShare) +
                    countShare(restoreFlowDataLocalRepository.customShare)
            }
            is OnboardingFlow.RestoreWallet.DevicePlusSocialShare -> {
                countShare(restoreFlowDataLocalRepository.deviceShare) +
                    countShare(restoreFlowDataLocalRepository.socialShare)
            }
            is OnboardingFlow.RestoreWallet.DevicePlusCustomShare -> {
                countShare(restoreFlowDataLocalRepository.deviceShare) +
                    countShare(restoreFlowDataLocalRepository.customShare)
            }
            else -> {
                0
            }
        }
        return sharesCount > 1
    }

    private fun countShare(share: Any?): Int {
        return if (share == null) 0 else 1
    }

    suspend fun tryRestoreUser(restoreFlow: OnboardingFlow.RestoreWallet): RestoreUserResult = try {
        when (restoreFlow) {
            is OnboardingFlow.RestoreWallet.SocialPlusCustomShare -> {
                tryRestoreUser(restoreFlow)
            }
            is OnboardingFlow.RestoreWallet.DevicePlusCustomShare -> {
                tryRestoreUser(restoreFlow)
            }
            is OnboardingFlow.RestoreWallet.DevicePlusSocialShare -> {
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
            gson.fromJsonReified<JsonObject>(it)
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

    private suspend fun tryRestoreUser(
        restoreFlow: OnboardingFlow.RestoreWallet.DevicePlusSocialShare
    ): RestoreUserResult = try {
        val deviceShare = restoreFlowDataLocalRepository.deviceShare
        val socialShare = restoreFlowDataLocalRepository.socialShare
        val socialShareUserId = restoreFlowDataLocalRepository.socialShareUserId
        if (deviceShare != null && socialShare != null) {
            val result: Web3AuthSignInResponse = web3AuthApi.triggerSignInNoCustom(socialShare, deviceShare)
            signUpDetailsStorage.save(
                data = Web3AuthSignUpResponse(
                    ethereumPublicKey = result.ethereumPublicKey,
                    mnemonicPhrase = result.mnemonicPhrase,
                    encryptedMnemonicPhrase = JsonObject(),
                    deviceShare = deviceShare,
                    customThirdShare = null
                ),
                userId = socialShareUserId.orEmpty()
            )
            restoreFlowDataLocalRepository.generateActualAccount(result.mnemonicPhrase.split(""))
        } else {
            throw IllegalStateException("Social+Device restore way failed. Social or Device share is null!")
        }
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
}
