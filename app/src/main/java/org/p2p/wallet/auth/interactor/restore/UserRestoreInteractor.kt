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
        object SocialPlusCustomShareWay : RestoreUserWay
    }

    fun isUserReadyToBeRestored(): Boolean {
        val isTwoSharesAvailable =
            restoreFlowDataLocalRepository.socialShare != null && restoreFlowDataLocalRepository.customShare != null
        return isTwoSharesAvailable
    }

    suspend fun tryRestoreUser(restoreWay: RestoreUserWay): RestoreUserResult = try {
        when (restoreWay) {
            is RestoreUserWay.SocialPlusCustomShareWay -> {
                val customShare = restoreFlowDataLocalRepository.customShare
                    ?: throw IllegalStateException("Social+Custom restore way failed. Third share is null")
                val socialShare = restoreFlowDataLocalRepository.socialShare
                    ?: throw  IllegalStateException("Social+Custom restore way failed. Social share is null")
                val socialShareUserId = restoreFlowDataLocalRepository.socialShareUserId
                    ?: throw  IllegalStateException("Social+Custom restore way failed. Social share ID is null")
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
