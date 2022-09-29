package org.p2p.wallet.auth.interactor.restore

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.RestoreUserResult
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.web3authsdk.GoogleSignInHelper
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthErrorResponse
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
    private val gson: Gson,
    private val googleSignInHelper: GoogleSignInHelper
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
            ?: error("Social+Custom restore way failed. Third share is null")
        val socialShare = restoreFlowDataLocalRepository.socialShare
            ?: error("Social+Custom restore way failed. Social share is null")
        val socialShareUserId = restoreFlowDataLocalRepository.socialShareUserId
            ?: error("Social+Custom restore way failed. Social share ID is null")

        val encryptedMnemonicGson = restoreFlowDataLocalRepository.encryptedMnemonic?.let {
            gson.fromJsonReified<JsonObject>(it)
        } ?: error("Device+Custom restore way failed. Mnemonic phrase is null")

        val result: Web3AuthSignInResponse =
            web3AuthApi.triggerSignInNoDevice(socialShare, customShare, encryptedMnemonicGson)

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
            ?: error("Device+Custom restore way failed. Third share is null")

        val encryptedMnemonicGson = restoreFlowDataLocalRepository.encryptedMnemonic?.let {
            gson.fromJsonReified<JsonObject>(it)
        } ?: error("Device+Custom restore way failed. Mnemonic phrase is null")
        val deviceShare = restoreFlowDataLocalRepository.deviceShare
        when {
            deviceShare == null -> {
                Timber.e("Restore Device + Custom. No Device share")
                RestoreUserResult.DeviceShareNotFound
            }
            googleSignInHelper.isGoogleTokenExpired() -> {
                Timber.e("Restore Device + Custom. Google Token expired")
                RestoreUserResult.SocialAuthRequired
            }
            else -> {
                Timber.e("Restore Device + Custom. Start restore wallet")
                val result: Web3AuthSignInResponse = web3AuthApi.triggerSignInNoTorus(
                    deviceShare = deviceShare,
                    thirdShare = customShare,
                    encryptedMnemonicPhrase = encryptedMnemonicGson
                )
                restoreFlowDataLocalRepository.generateActualAccount(result.mnemonicPhrase.split(""))
                RestoreUserResult.RestoreSuccessful
            }
        }
    } catch (web3AuthError: Web3AuthErrorResponse) {
        if (web3AuthError.errorType == Web3AuthErrorResponse.ErrorType.CANNOT_RECONSTRUCT) {
            RestoreUserResult.UserNotFound
        } else {
            RestoreUserResult.SharesDoNotMatch
            // TODO: PWN-5197 check on another error but use this for now
            // RestoreUserResult.RestoreFailed(Throwable("Unknown error type"))
        }
    } catch (error: Throwable) {
        RestoreUserResult.RestoreFailed(error)
    }

    private suspend fun tryRestoreUser(
        restoreFlow: OnboardingFlow.RestoreWallet.DevicePlusSocialShare
    ): RestoreUserResult = try {
        val deviceShare = restoreFlowDataLocalRepository.deviceShare
        val socialShare = restoreFlowDataLocalRepository.socialShare
        val socialShareUserId = restoreFlowDataLocalRepository.socialShareUserId
        val encryptedMnemonicGson = restoreFlowDataLocalRepository.encryptedMnemonic?.let {
            gson.fromJsonReified<JsonObject>(it)
        } ?: error("Device+Custom restore way failed. Mnemonic phrase is null")
        if (deviceShare != null && socialShare != null) {
            val result: Web3AuthSignInResponse =
                web3AuthApi.triggerSignInNoCustom(socialShare, deviceShare, encryptedMnemonicGson)
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
            error("Social+Device restore way failed. Social or Device share is null!")
        }
        RestoreUserResult.RestoreSuccessful
    } catch (web3AuthError: Web3AuthErrorResponse) {
        val socialShareId = restoreFlowDataLocalRepository.socialShareUserId.orEmpty()
        when (web3AuthError.errorType) {
            Web3AuthErrorResponse.ErrorType.CANNOT_RECONSTRUCT -> {
                RestoreUserResult.DeviceAndSocialShareNotMatch(socialShareId)
            }
            Web3AuthErrorResponse.ErrorType.SOCIAL_SHARE_NOT_FOUND -> {
                RestoreUserResult.SocialShareNotFound(socialShareId)
            }
            else -> {
                RestoreUserResult.RestoreFailed(web3AuthError)
            }
        }
    } catch (e: Throwable) {
        RestoreUserResult.RestoreFailed(e)
    }

    fun finishAuthFlow() {
        restoreFlowDataLocalRepository.userActualAccount?.also {
            tokenKeyProvider.secretKey = it.secretKey
            tokenKeyProvider.publicKey = it.publicKey.toBase58()
        } ?: error("User actual account is null, restoring a user is failed")
    }
}
