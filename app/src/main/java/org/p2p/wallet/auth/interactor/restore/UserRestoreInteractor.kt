package org.p2p.wallet.auth.interactor.restore

import com.google.gson.JsonObject
import timber.log.Timber
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.auth.interactor.UsernameInteractor
import org.p2p.wallet.auth.model.OnboardingFlow.RestoreWallet
import org.p2p.wallet.auth.model.RestoreError
import org.p2p.wallet.auth.model.RestoreUserException
import org.p2p.wallet.auth.model.RestoreUserResult
import org.p2p.wallet.auth.model.RestoreUserResult.RestoreFailure
import org.p2p.wallet.auth.model.RestoreUserResult.RestoreSuccess
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.web3authsdk.Web3AuthApi
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthErrorResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignInResponse
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseProvider
import org.p2p.wallet.infrastructure.network.provider.SeedPhraseSource
import org.p2p.wallet.infrastructure.network.provider.TokenKeyProvider
import org.p2p.wallet.utils.toBase58Instance

class UserRestoreInteractor(
    private val web3AuthApi: Web3AuthApi,
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository,
    private val signUpDetailsStorage: UserSignUpDetailsStorage,
    private val tokenKeyProvider: TokenKeyProvider,
    private val seedPhraseProvider: SeedPhraseProvider,
    private val usernameInteractor: UsernameInteractor
) {

    suspend fun tryRestoreUser(restoreFlow: RestoreWallet): RestoreUserResult {
        Timber.i("Started to restore user: ${restoreFlow::class.simpleName}")
        return when (restoreFlow) {
            is RestoreWallet.SocialPlusCustomShare -> tryRestoreUser(restoreFlow)
            is RestoreWallet.DevicePlusCustomShare -> tryRestoreUser(restoreFlow)
            is RestoreWallet.DevicePlusSocialShare -> tryRestoreUser(restoreFlow)
            is RestoreWallet.DevicePlusCustomOrSocialPlusCustom -> tryRestoreUser(restoreFlow)
            is RestoreWallet.DevicePlusSocialOrSocialPlusCustom -> tryRestoreUser(restoreFlow)
            else -> error("Unknown restore flow")
        }
    }

    private suspend fun tryRestoreUser(
        restoreWay: RestoreWallet.SocialPlusCustomShare
    ): RestoreUserResult = try {
        val customShare = restoreFlowDataLocalRepository.customShare
            ?: error("Social+Custom restore way failed. Third share is null")
        val encryptedMnemonic = restoreFlowDataLocalRepository.encryptedMnemonicJson
            ?: error("Social+Custom restore way failed. Mnemonic phrase is null")

        val torusKey = restoreFlowDataLocalRepository.torusKey
        val socialShareUserId = restoreFlowDataLocalRepository.socialShareUserId

        val deviceShare = restoreFlowDataLocalRepository.deviceShare
        if (torusKey.isNullOrEmpty() && socialShareUserId.isNullOrEmpty()) {
            Timber.i(RestoreError("Failed to restore with SocialPlusCustomShare"))
            RestoreFailure.SocialPlusCustomShare.TorusKeyNotFound
        } else {
            val result: Web3AuthSignInResponse = web3AuthApi.triggerSignInNoDevice(
                torusKey = torusKey!!,
                thirdShare = customShare,
                encryptedMnemonic = encryptedMnemonic
            )

            signUpDetailsStorage.save(
                data = Web3AuthSignUpResponse(
                    ethereumPublicKey = result.ethereumPublicKey,
                    mnemonicPhrase = result.mnemonicPhrase,
                    encryptedMnemonicPhrase = JsonObject(),
                    deviceShare = deviceShare,
                    customThirdShare = customShare
                ),
                userId = socialShareUserId!!,
                isCreate = false
            )

            restoreFlowDataLocalRepository.generateActualAccount(result.mnemonicPhraseWords)
            seedPhraseProvider.setUserSeedPhrase(
                words = result.mnemonicPhraseWords,
                provider = SeedPhraseSource.WEB_AUTH
            )

            RestoreSuccess.SocialPlusCustomShare
        }
    } catch (error: Web3AuthErrorResponse) {
        when (error.errorType) {
            Web3AuthErrorResponse.ErrorType.SOCIAL_SHARE_NOT_FOUND -> {
                val userEmailAddress = restoreFlowDataLocalRepository.socialShareUserId.orEmpty()
                // Reset torus and socialId, cause we not found wallet with this torus key
                restoreFlowDataLocalRepository.torusKey = null
                restoreFlowDataLocalRepository.socialShareUserId = null
                RestoreFailure.SocialPlusCustomShare.SocialShareNotFound(userEmailAddress)
            }
            Web3AuthErrorResponse.ErrorType.CANNOT_RECONSTRUCT -> {
                val userEmailAddress = restoreFlowDataLocalRepository.socialShareUserId.orEmpty()
                RestoreFailure.SocialPlusCustomShare.SocialShareNotMatch(userEmailAddress)
            }
            else -> {
                val errorMessage = error.message.orEmpty()
                RestoreFailure.SocialPlusCustomShare(
                    RestoreUserException(
                        errorMessage,
                        error.errorCode
                    )
                )
            }
        }
    } catch (e: Throwable) {
        val errorMessage = e.message.orEmpty()
        RestoreFailure.SocialPlusCustomShare(RestoreUserException(errorMessage))
    }

    private suspend fun tryRestoreUser(
        restoreFlow: RestoreWallet.DevicePlusCustomShare
    ): RestoreUserResult = try {
        val customShare = restoreFlowDataLocalRepository.customShare
            ?: error("Device+Custom restore way failed. Third share is null")
        val encryptedMnemonic = restoreFlowDataLocalRepository.encryptedMnemonicJson
            ?: error("Device+Custom restore way failed. Mnemonic phrase is null")
        val deviceShare = restoreFlowDataLocalRepository.deviceShare

        if (deviceShare == null) {
            RestoreFailure.DevicePlusCustomShare(RestoreUserException("No Device Share"))
        } else {
            Timber.i("Restore Device + Custom. Start restore wallet")
            val result: Web3AuthSignInResponse = web3AuthApi.triggerSignInNoTorus(
                deviceShare = deviceShare,
                thirdShare = customShare,
                encryptedMnemonic = encryptedMnemonic
            )
            restoreFlowDataLocalRepository.generateActualAccount(result.mnemonicPhraseWords)

            seedPhraseProvider.setUserSeedPhrase(
                words = result.mnemonicPhraseWords,
                provider = SeedPhraseSource.WEB_AUTH
            )
            RestoreSuccess.DevicePlusCustomShare
        }
    } catch (web3AuthError: Web3AuthErrorResponse) {
        if (web3AuthError.errorType == Web3AuthErrorResponse.ErrorType.CANNOT_RECONSTRUCT) {
            RestoreFailure.DevicePlusCustomShare.SharesDoesNotMatch
        } else {
            RestoreFailure.DevicePlusCustomShare.UserNotFound
            // TODO: PWN-5197 check on another error but use this for now
            // RestoreUserResult.RestoreFailed(Throwable("Unknown error type"))
        }
    } catch (error: Throwable) {
        RestoreFailure.DevicePlusCustomShare(RestoreUserException(error.message.orEmpty()))
    }

    private suspend fun tryRestoreUser(
        restoreFlow: RestoreWallet.DevicePlusSocialShare
    ): RestoreUserResult = try {
        val deviceShare = restoreFlowDataLocalRepository.deviceShare
            ?: error("Device+Social restore way failed. Device share is null")
        val torusKey = restoreFlowDataLocalRepository.torusKey
            ?: error("Device+Social restore way failed. Social share is null")
        val socialShareUserId = restoreFlowDataLocalRepository.socialShareUserId
            ?: error("Device+Social restore way failed. Social share ID is null")

        val result: Web3AuthSignInResponse = web3AuthApi.triggerSignInNoCustom(
            torusKey = torusKey,
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
            userId = socialShareUserId,
            isCreate = false
        )
        restoreFlowDataLocalRepository.generateActualAccount(result.mnemonicPhraseWords)

        seedPhraseProvider.setUserSeedPhrase(
            words = result.mnemonicPhraseWords,
            provider = SeedPhraseSource.WEB_AUTH
        )
        RestoreSuccess.DevicePlusSocialShare
    } catch (web3AuthError: Web3AuthErrorResponse) {
        val socialShareId = restoreFlowDataLocalRepository.socialShareUserId.orEmpty()
        when (web3AuthError.errorType) {
            Web3AuthErrorResponse.ErrorType.CANNOT_RECONSTRUCT -> {
                RestoreFailure.DevicePlusSocialShare.DeviceAndSocialShareNotMatch(socialShareId)
            }
            Web3AuthErrorResponse.ErrorType.SOCIAL_SHARE_NOT_FOUND -> {
                RestoreFailure.DevicePlusSocialShare.SocialShareNotFound(socialShareId)
            }
            else -> {
                RestoreFailure.DevicePlusSocialShare(
                    RestoreUserException(web3AuthError.message.orEmpty(), web3AuthError.errorCode)
                )
            }
        }
    } catch (e: Throwable) {
        val errorMessage = e.message.orEmpty()
        RestoreFailure.DevicePlusSocialShare(RestoreUserException(errorMessage))
    }

    private suspend fun tryRestoreUser(
        restoreFlow: RestoreWallet.DevicePlusSocialOrSocialPlusCustom
    ): RestoreUserResult {
        var result: RestoreUserResult

        // First case try to restore with DEVICE + SOCIAL
        result = tryRestoreUser(RestoreWallet.DevicePlusSocialShare)

        if (result is RestoreFailure) {
            // if restore was failed
            // Try last try with SOCIAL + CUSTOM
            result = tryRestoreUser(RestoreWallet.SocialPlusCustomShare)
            // if restore is FAILED we throw error thast SHARES ARE NOT MATCH
            // otherwise Restore is SUCCESSFULL
        }
        return if (result is RestoreFailure) {
            RestoreFailure.DevicePlusSocialOrSocialPlusCustom(
                RestoreUserException("Shares are not match")
            )
        } else {
            result
        }
    }

    private suspend fun tryRestoreUser(
        restoreFlow: RestoreWallet.DevicePlusCustomOrSocialPlusCustom
    ): RestoreUserResult {
        // First case try to restore with DEVICE + CUSTOM
        var result: RestoreUserResult
        result = tryRestoreUser(RestoreWallet.DevicePlusCustomShare)

        if (result is RestoreFailure) {
            // if restore was failed
            // try last try with SOCIAL + CUSTOM
            result = tryRestoreUser(RestoreWallet.SocialPlusCustomShare)
        }

        // if restore is FAILED we throw error thast SHARES ARE NOT MATCH
        // otherwise Restore is SUCCESSFULL
        return if (result is RestoreFailure) {
            RestoreFailure.DevicePlusCustomOrSocialPlusCustom(
                RestoreUserException("Shares are not match")
            )
        } else {
            result
        }
    }

    suspend fun finishAuthFlow() {
        restoreFlowDataLocalRepository.userActualAccount?.also {
            tokenKeyProvider.keyPair = it.keypair
            tokenKeyProvider.publicKey = it.publicKey.toBase58()
        } ?: error("User actual account is null, restoring a user is failed")

        usernameInteractor.tryRestoreUsername(tokenKeyProvider.publicKey.toBase58Instance())
    }

    suspend fun refreshDeviceShare(
        metadata: GatewayOnboardingMetadata
    ): Web3AuthSignUpResponse.ShareDetailsWithMeta {
        val socialShareUserId = restoreFlowDataLocalRepository.socialShareUserId
            ?: error("Device+Social restore way failed. Social share ID is null")
        val deviceShare = web3AuthApi.refreshDeviceShare(metadata)
        val data = signUpDetailsStorage.getLastSignUpUserDetails()!!.signUpDetails.copy(deviceShare = deviceShare)
        signUpDetailsStorage.save(
            data = data,
            userId = socialShareUserId,
            isCreate = false
        )
        return deviceShare
    }
}
