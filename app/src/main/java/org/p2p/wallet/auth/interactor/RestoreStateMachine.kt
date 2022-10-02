package org.p2p.wallet.auth.interactor

import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.OnboardingFlow

// Temporary solution of state machine, which allows us to restart restore with another type of restore option
class RestoreStateMachine(private val restoreInteractor: RestoreWalletInteractor) {
    private val restoreFlowFailureMap = mutableMapOf<OnboardingFlow.RestoreWallet, Boolean>()

    fun onRestoreFailure(flow: OnboardingFlow.RestoreWallet) {
        restoreFlowFailureMap[flow] = true
        countTriesLeft()
    }

    init {
        reset()
    }

    fun reset() {
        restoreFlowFailureMap.clear()
        if (!restoreInteractor.isDeviceShareSaved()) {
            restoreFlowFailureMap[OnboardingFlow.RestoreWallet.DevicePlusCustomShare] = true
            restoreFlowFailureMap[OnboardingFlow.RestoreWallet.DevicePlusSocialShare] = true
        }
    }

    fun isRestoreAvailable(): Boolean = countTriesLeft() > 0

    fun getAvailableRestoreWithSocialShare(): OnboardingFlow.RestoreWallet? {
        val isDeviceShareExist = restoreInteractor.isDeviceShareSaved()
        return when {
            restoreFlowFailureMap[OnboardingFlow.RestoreWallet.DevicePlusSocialShare] == true &&
                restoreFlowFailureMap[OnboardingFlow.RestoreWallet.SocialPlusCustomShare] == true -> {
                null
            }
            isDeviceShareExist && restoreFlowFailureMap[OnboardingFlow.RestoreWallet.DevicePlusSocialShare] == null -> {
                OnboardingFlow.RestoreWallet.DevicePlusSocialShare
            }
            else -> OnboardingFlow.RestoreWallet.SocialPlusCustomShare
        }
    }

    fun getAvailableRestoreWithCustomShare(): OnboardingFlow.RestoreWallet? {
        val isDeviceShareExist = restoreInteractor.isDeviceShareSaved()
        return when {
            restoreFlowFailureMap[OnboardingFlow.RestoreWallet.DevicePlusCustomShare] == true &&
                restoreFlowFailureMap[OnboardingFlow.RestoreWallet.SocialPlusCustomShare] == true -> {
                null
            }
            isDeviceShareExist && restoreFlowFailureMap[OnboardingFlow.RestoreWallet.DevicePlusCustomShare] == null -> {
                OnboardingFlow.RestoreWallet.DevicePlusCustomShare
            }
            else -> OnboardingFlow.RestoreWallet.SocialPlusCustomShare
        }
    }

    private fun countTriesLeft(): Int {
        return listOf(
            OnboardingFlow.RestoreWallet.SocialPlusCustomShare,
            OnboardingFlow.RestoreWallet.DevicePlusSocialShare,
            OnboardingFlow.RestoreWallet.DevicePlusCustomShare
        ).map {
            restoreFlowFailureMap[it]
        }.filter { it == null }.size
    }
}
