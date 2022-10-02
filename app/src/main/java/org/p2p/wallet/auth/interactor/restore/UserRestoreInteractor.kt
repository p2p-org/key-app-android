package org.p2p.wallet.auth.interactor.restore

import com.google.gson.JsonObject
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.RestoreUserException
import org.p2p.wallet.auth.model.RestoreUserResult
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthErrorResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignInResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import timber.log.Timber

class UserRestoreInteractor(
    private val web3AuthApi: Web3AuthApi,
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository,
    private val signUpDetailsStorage: UserSignUpDetailsStorage,
    private val tokenKeyProvider: TokenKeyProvider
) {

    fun isUserReadyToBeRestored(restoreFlow: OnboardingFlow.RestoreWallet): Boolean {
        val sharesCount = when (restoreFlow) {
            is OnboardingFlow.RestoreWallet.SocialPlusCustomShare -> {
                countShare(restoreFlowDataLocalRepository.torusKey) +
                    countShare(restoreFlowDataLocalRepository.customShare)
            }
            is OnboardingFlow.RestoreWallet.DevicePlusSocialShare -> {
                countShare(restoreFlowDataLocalRepository.deviceShare) +
                    countShare(restoreFlowDataLocalRepository.torusKey)
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

    suspend fun tryRestoreUser(restoreFlow: OnboardingFlow.RestoreWallet): RestoreUserResult = when (restoreFlow) {
        is OnboardingFlow.RestoreWallet.SocialPlusCustomShare -> tryRestoreUser(restoreFlow)
        is OnboardingFlow.RestoreWallet.DevicePlusCustomShare -> tryRestoreUser(restoreFlow)
        is OnboardingFlow.RestoreWallet.DevicePlusSocialShare -> tryRestoreUser(restoreFlow)
        else -> error("Unknown restore flow")
    }

    private suspend fun tryRestoreUser(
        restoreWay: OnboardingFlow.RestoreWallet.SocialPlusCustomShare
    ): RestoreUserResult = try {
        val customShare = restoreFlowDataLocalRepository.customShare
            ?: error("Social+Custom restore way failed. Third share is null")
        val torusKey = restoreFlowDataLocalRepository.torusKey
        val socialShareUserId = restoreFlowDataLocalRepository.socialShareUserId
        val encryptedMnemonic = restoreFlowDataLocalRepository.encryptedMnemonicJson
            ?: error("Social+Custom restore way failed. Mnemonic phrase is null")

        if (torusKey.isNullOrEmpty() && socialShareUserId.isNullOrEmpty()) {
            RestoreUserResult.RestoreFailure.SocialPlusCustomShare.TorusKeyNotFound
        } else {
            val result: Web3AuthSignInResponse = web3AuthApi.triggerSignInNoDevice(
                socialShare = torusKey!!,
                thirdShare = customShare,
                encryptedMnemonic = encryptedMnemonic
            )
            signUpDetailsStorage.save(
                data = Web3AuthSignUpResponse(
                    ethereumPublicKey = result.ethereumPublicKey,
                    mnemonicPhrase = result.mnemonicPhrase,
                    encryptedMnemonicPhrase = JsonObject(),
                    deviceShare = null,
                    customThirdShare = customShare
                ),
                userId = socialShareUserId!!
            )

            restoreFlowDataLocalRepository.generateActualAccount(result.mnemonicPhraseWords)
            RestoreUserResult.RestoreSuccess.SocialPlusCustomShare
        }
    } catch (error: Web3AuthErrorResponse) {
        val errorMessage = error.message.orEmpty()
        RestoreUserResult.RestoreFailure.SocialPlusCustomShare(RestoreUserException(errorMessage, error.errorCode))
    } catch (e: Throwable) {
        val errorMessage = e.message.orEmpty()
        RestoreUserResult.RestoreFailure.SocialPlusCustomShare(RestoreUserException(errorMessage))
    }

    private suspend fun tryRestoreUser(
        restoreFlow: OnboardingFlow.RestoreWallet.DevicePlusCustomShare
    ): RestoreUserResult = try {
        val customShare = restoreFlowDataLocalRepository.customShare
            ?: error("Device+Custom restore way failed. Third share is null")
        val encryptedMnemonic = restoreFlowDataLocalRepository.encryptedMnemonicJson
            ?: error("Device+Custom restore way failed. Mnemonic phrase is null")
        val deviceShare = restoreFlowDataLocalRepository.deviceShare

        when {
            deviceShare == null -> {
                RestoreUserResult.RestoreFailure.DevicePlusCustomShare(RestoreUserException("No Device Share"))
            }
            else -> {
                Timber.i("Restore Device + Custom. Start restore wallet")
                val result: Web3AuthSignInResponse = web3AuthApi.triggerSignInNoTorus(
                    deviceShare = deviceShare,
                    thirdShare = customShare,
                    encryptedMnemonic = encryptedMnemonic
                )
                restoreFlowDataLocalRepository.generateActualAccount(result.mnemonicPhraseWords)
                RestoreUserResult.RestoreSuccess.DevicePlusCustomShare
            }
        }
    } catch (web3AuthError: Web3AuthErrorResponse) {
        if (web3AuthError.errorType == Web3AuthErrorResponse.ErrorType.CANNOT_RECONSTRUCT) {
            RestoreUserResult.RestoreFailure.DevicePlusCustomShare.UserNotFound
        } else {
            RestoreUserResult.RestoreFailure.DevicePlusCustomShare.SharesDoesNotMatch
            // TODO: PWN-5197 check on another error but use this for now
            // RestoreUserResult.RestoreFailed(Throwable("Unknown error type"))
        }
    } catch (error: Throwable) {
        RestoreUserResult.RestoreFailure.DevicePlusCustomShare(RestoreUserException(error.message.orEmpty()))
    }

    private suspend fun tryRestoreUser(
        restoreFlow: OnboardingFlow.RestoreWallet.DevicePlusSocialShare
    ): RestoreUserResult = try {
        val deviceShare = restoreFlowDataLocalRepository.deviceShare
            ?: error("Device+Social restore way failed. Device share is null")
        val torusKey = restoreFlowDataLocalRepository.torusKey
            ?: error("Device+Social restore way failed. Social share is null")
        val socialShareUserId = restoreFlowDataLocalRepository.socialShareUserId
            ?: error("Device+Social restore way failed. Social share ID is null")

        val result: Web3AuthSignInResponse = web3AuthApi.triggerSignInNoCustom(
            socialShare = torusKey,
            deviceShare = deviceShare
        )
        signUpDetailsStorage.save(
            data = Web3AuthSignUpResponse(
                ethereumPublicKey = result.ethereumPublicKey,
                mnemonicPhrase = result.mnemonicPhrase,
                encryptedMnemonicPhrase = JsonObject(),
                deviceShare = deviceShare,
                customThirdShare = null
            ),
            userId = socialShareUserId
        )
        restoreFlowDataLocalRepository.generateActualAccount(result.mnemonicPhraseWords)
        RestoreUserResult.RestoreSuccess.DevicePlusSocialShare
    } catch (web3AuthError: Web3AuthErrorResponse) {
        val socialShareId = restoreFlowDataLocalRepository.socialShareUserId.orEmpty()
        when (web3AuthError.errorType) {
            Web3AuthErrorResponse.ErrorType.CANNOT_RECONSTRUCT -> {
                RestoreUserResult.RestoreFailure.DevicePlusSocialShare.DeviceAndSocialShareNotMatch(socialShareId)
            }
            Web3AuthErrorResponse.ErrorType.SOCIAL_SHARE_NOT_FOUND -> {
                RestoreUserResult.RestoreFailure.DevicePlusSocialShare.SocialShareNotFound(socialShareId)
            }
            else -> {
                RestoreUserResult.RestoreFailure.DevicePlusSocialShare(
                    RestoreUserException(web3AuthError.message.orEmpty())
                )
            }
        }
    } catch (e: Throwable) {
        RestoreUserResult.RestoreFailure.DevicePlusSocialShare(RestoreUserException(e.message.orEmpty()))
    }

    fun finishAuthFlow() {
        restoreFlowDataLocalRepository.userActualAccount?.also {
            tokenKeyProvider.secretKey = it.secretKey
            tokenKeyProvider.publicKey = it.publicKey.toBase58()
        } ?: error("User actual account is null, restoring a user is failed")
    }
}
