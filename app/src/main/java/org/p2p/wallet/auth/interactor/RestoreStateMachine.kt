package org.p2p.wallet.auth.interactor

import org.p2p.wallet.auth.model.OnboardingFlow.RestoreWallet
import timber.log.Timber

// Temporary solution of state machine, which allows us to restart restore with another type of restore option
class RestoreStateMachine {
    private val restoreFlowFailureMap = mutableMapOf<RestoreWallet, Boolean>()

    fun onRestoreFailure(flow: RestoreWallet) {
        restoreFlowFailureMap[flow] = true
        countTriesLeft()
    }

    var isDeviceShareSaved: Boolean = false
        set(value) {
            Timber.i("Update device share flag: $value")
            field = value
            reset()
        }

    fun reset() {
        restoreFlowFailureMap.clear()
        if (!isDeviceShareSaved) {
            restoreFlowFailureMap[RestoreWallet.DevicePlusCustomShare] = true
            restoreFlowFailureMap[RestoreWallet.DevicePlusSocialShare] = true
        }
    }

    fun isRestoreAvailable(): Boolean = countTriesLeft() > 0

    fun getAvailableRestoreWithSocialShare(): RestoreWallet? {
        return when {
            restoreFlowFailureMap[RestoreWallet.DevicePlusSocialShare] == true &&
                restoreFlowFailureMap[RestoreWallet.SocialPlusCustomShare] == true -> {
                null
            }
            isDeviceShareSaved && restoreFlowFailureMap[RestoreWallet.DevicePlusSocialShare] == null -> {
                RestoreWallet.DevicePlusSocialShare
            }
            else -> {
                RestoreWallet.SocialPlusCustomShare
            }
        }
    }

    fun getAvailableRestoreWithCustomShare(): RestoreWallet? {
        return when {
            restoreFlowFailureMap[RestoreWallet.DevicePlusCustomShare] == true &&
                restoreFlowFailureMap[RestoreWallet.SocialPlusCustomShare] == true -> {
                null
            }
            isDeviceShareSaved && restoreFlowFailureMap[RestoreWallet.DevicePlusCustomShare] == null -> {
                RestoreWallet.DevicePlusCustomShare
            }
            else -> {
                RestoreWallet.SocialPlusCustomShare
            }
        }
    }

    private fun countTriesLeft(): Int {
        return listOf(
            RestoreWallet.SocialPlusCustomShare,
            RestoreWallet.DevicePlusSocialShare,
            RestoreWallet.DevicePlusCustomShare
        )
            .map { restoreFlowFailureMap[it] }
            .filter { it == null }
            .size
    }
}
