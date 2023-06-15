package org.p2p.wallet.auth.interactor.restore

import org.p2p.wallet.auth.analytics.RestoreWalletAnalytics
import org.p2p.wallet.auth.analytics.RestoreWalletAnalytics.UsernameRestoreMethod
import org.p2p.wallet.auth.gateway.repository.model.GatewayOnboardingMetadata
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.PhoneNumber
import org.p2p.wallet.auth.model.RestoreUserResult
import org.p2p.wallet.auth.repository.RestoreFlowDataLocalRepository
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.web3authsdk.response.Web3AuthSignUpResponse
import org.p2p.wallet.smsinput.SmsInputTimer

class RestoreWalletInteractor(
    private val customShareRestoreInteractor: CustomShareRestoreInteractor,
    private val torusKeyInteractor: TorusKeyInteractor,
    private val userRestoreInteractor: UserRestoreInteractor,
    private val restoreFlowDataLocalRepository: RestoreFlowDataLocalRepository,
    private val smsInputTimer: SmsInputTimer,
    private val signUpDetailsStorage: UserSignUpDetailsStorage,
    private val restoreWalletAnalytics: RestoreWalletAnalytics
) {

    suspend fun startRestoreCustomShare(userPhoneNumber: PhoneNumber, isResend: Boolean = false) =
        customShareRestoreInteractor.startRestoreCustomShare(userPhoneNumber, isResend)

    fun getUserPhoneNumber() = restoreFlowDataLocalRepository.userPhoneNumber

    fun getUserEmailAddress() = signUpDetailsStorage.getLastSignUpUserDetails()?.userId

    fun generateRestoreUserKeyPair() {
        restoreFlowDataLocalRepository.generateRestoreUserKeyPair()
    }

    suspend fun obtainTorusKey(userId: String, idToken: String) {
        torusKeyInteractor.getTorusKey(idToken, userId)
    }

    suspend fun finishRestoreCustomShare(smsCode: String) =
        customShareRestoreInteractor.finishRestoreCustomShare(smsCode)

    suspend fun tryRestoreUser(restoreFlow: OnboardingFlow.RestoreWallet): RestoreUserResult =
        userRestoreInteractor.tryRestoreUser(restoreFlow)

    suspend fun refreshDeviceShare(
        metadata: GatewayOnboardingMetadata
    ): Web3AuthSignUpResponse.ShareDetailsWithMeta = userRestoreInteractor.refreshDeviceShare(metadata)

    suspend fun finishAuthFlow() {
        restoreWalletAnalytics.setUserRestoreMethod(UsernameRestoreMethod.WEB3AUTH)
        userRestoreInteractor.finishAuthFlow()
    }

    fun setIsRestoreWalletRequestSent(isSent: Boolean) {
        customShareRestoreInteractor.setIsRestoreWalletRequestSent(isSent)
    }

    fun isDeviceShareSaved(): Boolean = signUpDetailsStorage.isDeviceShareSaved()

    fun getUserEnterPhoneNumberTriesCount() = restoreFlowDataLocalRepository.userPhoneNumberEnteredCount

    fun resetUserEnterPhoneNumberTriesCount() {
        restoreFlowDataLocalRepository.resetUserPhoneNumberEnteredCount()
    }

    fun resetUserPhoneNumber() {
        restoreFlowDataLocalRepository.userPhoneNumber = null
        resetTimer()
        resetUserEnterPhoneNumberTriesCount()
    }

    private fun resetTimer() {
        smsInputTimer.resetSmsCount()
    }
}
