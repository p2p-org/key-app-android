package org.p2p.wallet.auth.ui.restore.common

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.RestoreUserResult
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber

class CommonRestorePresenter(
    private val onboardingInteractor: OnboardingInteractor,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val accountStorageContract: UserSignUpDetailsStorage
) : BasePresenter<CommonRestoreContract.View>(), CommonRestoreContract.Presenter {

    override fun useGoogleAccount() {
        view?.startGoogleFlow()
    }

    override fun useCustomShare() {
        onboardingInteractor.currentFlow = OnboardingFlow.RestoreWallet.DevicePlusCustomShare
        view?.navigateToPhoneEnter()
    }

    override fun switchFlowToRestore() {
        onboardingInteractor.currentFlow = OnboardingFlow.RestoreWallet()
        restoreWalletInteractor.generateRestoreUserKeyPair()
        view?.setGoogleFlowEnabled(accountStorageContract.isDeviceShareSaved())
    }

    override fun setGoogleIdToken(userId: String, idToken: String) {
        view?.setLoadingState(isScreenLoading = true)
        restoreWalletInteractor.restoreSocialShare(userId, idToken)
        if (restoreWalletInteractor.isUserReadyToBeRestored(OnboardingFlow.RestoreWallet.DevicePlusSocialShare)) {
            launch {
                restoreUserWithShares()
            }
        } else {
            view?.onNoTokenFoundError(userId)
            view?.setLoadingState(isScreenLoading = false)
        }
    }

    private suspend fun restoreUserWithShares() {
        when (val result = restoreWalletInteractor.tryRestoreUser(OnboardingFlow.RestoreWallet.DevicePlusSocialShare)) {
            is RestoreUserResult.RestoreSuccessful -> {
                restoreWalletInteractor.finishAuthFlow()
                view?.setLoadingState(isScreenLoading = false)
                view?.navigateToPinCreate()
            }
            is RestoreUserResult.RestoreFailed -> {
                Timber.e(result)
                view?.setLoadingState(isScreenLoading = false)
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
            RestoreUserResult.UserNotFound -> {
                view?.onNoTokenFoundError(restoreWalletInteractor.getUserEmailAddress().orEmpty())
            }
        }
    }
}
