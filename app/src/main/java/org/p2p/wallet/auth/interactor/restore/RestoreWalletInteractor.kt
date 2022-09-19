package org.p2p.wallet.auth.interactor.restore

import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository

class RestoreWalletInteractor(
    private val customShareRestoreInteractor: CustomShareRestoreInteractor,
    private val socialShareRestoreInteractor: SocialShareRestoreInteractor,
    private val userRestoreInteractor: UserRestoreInteractor,
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository
) {

    suspend fun startRestoreCustomShare(userPhoneNumber: PhoneNumber, isResend: Boolean = false) =
        customShareRestoreInteractor.startRestoreCustomShare(userPhoneNumber, isResend)

    fun getUserPhoneNumber() = restoreFlowDataLocalRepository.userPhoneNumber

    fun getUserEmailAddress() = restoreFlowDataLocalRepository.socialShareUserId

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
}
