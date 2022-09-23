package org.p2p.wallet.auth.interactor.restore

import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage

class RestoreWalletInteractor(
    private val customShareRestoreInteractor: CustomShareRestoreInteractor,
    private val socialShareRestoreInteractor: SocialShareRestoreInteractor,
    private val userRestoreInteractor: UserRestoreInteractor,
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository,
    private val signUpDetailsStorage: UserSignUpDetailsStorage
) {

    suspend fun startRestoreCustomShare(userPhoneNumber: PhoneNumber, isResend: Boolean = false) =
        customShareRestoreInteractor.startRestoreCustomShare(userPhoneNumber, isResend)

    fun getUserPhoneNumber() = restoreFlowDataLocalRepository.userPhoneNumber

    fun getUserEmailAddress() = signUpDetailsStorage.getLastSignUpUserDetails()?.userId

    fun generateRestoreUserKeyPair() {
        restoreFlowDataLocalRepository.generateRestoreUserKeyPair()
    }

    fun restoreSocialShare(userId: String, idToken: String) {
        socialShareRestoreInteractor.restoreSocialShare(idToken, userId)
    }

    fun isUserReadyToBeRestored(restoreFlow: OnboardingFlow.RestoreWallet) =
        userRestoreInteractor.isUserReadyToBeRestored(restoreFlow)

    suspend fun finishRestoreCustomShare(smsCode: String) =
        customShareRestoreInteractor.finishRestoreCustomShare(smsCode)

    suspend fun tryRestoreUser(restoreFlow: OnboardingFlow.RestoreWallet) =
        userRestoreInteractor.tryRestoreUser(restoreFlow)

    suspend fun finishAuthFlow() = userRestoreInteractor.finishAuthFlow()

    fun setIsRestoreWalletRequestSent(isSent: Boolean) {
        customShareRestoreInteractor.setIsRestoreWalletRequestSent(isSent)
    }

    fun isDeviceShareSaved(): Boolean = signUpDetailsStorage.isDeviceShareSaved()

    fun getUserEnterPhoneNumberTriesCount() = restoreFlowDataLocalRepository.userPhoneNumberEnteredCount

    fun resetUserEnterPhoneNumberTriesCount() {
        restoreFlowDataLocalRepository.resetUserPhoneNumberEnteredCount()
    }
}
