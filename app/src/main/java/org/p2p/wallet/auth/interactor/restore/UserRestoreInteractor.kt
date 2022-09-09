package org.p2p.wallet.auth.interactor.restore

import com.google.gson.JsonObject
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignInResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider

class UserRestoreInteractor(
    private val web3AuthApi: Web3AuthApi,
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository,
    private val signUpDetailsStorage: UserSignUpDetailsStorage,
    private val tokenKeyProvider: TokenKeyProvider
) {
    sealed interface RestoreUserResult {
        object RestoreSuccessful : RestoreUserResult
        class RestoreFailed(override val cause: Throwable) : Error(), RestoreUserResult
    }

    sealed interface RestoreUserWay {
        object DevicePlusSocialShareWay : RestoreUserWay
        object SocialPlusCustomShareWay : RestoreUserWay
    }

    fun isUserReadyToBeRestored(): Boolean {
        val deviceShareCount = countShare(restoreFlowDataLocalRepository.deviceShare) +
            countShare(restoreFlowDataLocalRepository.socialShare) +
            countShare(restoreFlowDataLocalRepository.customShare)
        return deviceShareCount > 1
    }

    fun isUserReadyToBeRestoredByPhone(): Boolean {
        val deviceShareCount = countShare(restoreFlowDataLocalRepository.socialShare) +
            countShare(restoreFlowDataLocalRepository.customShare)
        return deviceShareCount > 1
    }

    private fun countShare(share: Any?): Int {
        return if (share == null) 0 else 1
    }

    suspend fun tryRestoreUser(restoreWay: RestoreUserWay): RestoreUserResult = try {
        when (restoreWay) {
            is RestoreUserWay.DevicePlusSocialShareWay -> {
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
            }
            is RestoreUserWay.SocialPlusCustomShareWay -> {
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
            }
        }
        RestoreUserResult.RestoreSuccessful
    } catch (error: Throwable) {
        RestoreUserResult.RestoreFailed(error)
    }

    fun finishAuthFlow() {
        restoreFlowDataLocalRepository.userActualAccount?.also {
            tokenKeyProvider.secretKey = it.secretKey
            tokenKeyProvider.publicKey = it.publicKey.toBase58()
        } ?: throw NullPointerException("User actual account is null, restoring a user is failed")
    }
}
