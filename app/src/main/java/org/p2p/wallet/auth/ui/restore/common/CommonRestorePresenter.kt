package org.p2p.wallet.auth.ui.restore.common

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.model.RestoreSuccessState
import org.p2p.wallet.auth.repository.RestoreUserExceptionHandler
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.common.mvp.BasePresenter

class CommonRestorePresenter(
    private val onboardingInteractor: OnboardingInteractor,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val accountStorageContract: UserSignUpDetailsStorage,
    private val restoreUserExceptionHandler: RestoreUserExceptionHandler
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
        view?.setRestoreViaGoogleFlowVisibility(
            isVisible = accountStorageContract.isDeviceShareSaved() && !accountStorageContract.isSignUpInProcess()
        )
    }

    override fun setGoogleIdToken(userId: String, idToken: String) {
        launch {
            view?.setLoadingState(isScreenLoading = true)
            restoreWalletInteractor.obtainTorusKey(userId = userId, idToken = idToken)
            restoreUserWithShares()
            view?.setLoadingState(isScreenLoading = false)
        }
    }

    private suspend fun restoreUserWithShares() {
        val restoreType = OnboardingFlow.RestoreWallet.DevicePlusSocialShare
        onboardingInteractor.currentFlow = restoreType
        val restoreResult = restoreWalletInteractor.tryRestoreUser(restoreType)
        when (val restoreHandledState = restoreUserExceptionHandler.handleRestoreResult(restoreResult)) {
            is RestoreSuccessState -> {
                view?.navigateToPinCreate()
            }
            is RestoreFailureState.TitleSubtitleError -> {
                view?.showRestoreErrorScreen(restoreHandledState)
            }
            is RestoreFailureState.ToastError -> {
                view?.showUiKitSnackBar(message = restoreHandledState.message)
            }
        }
    }
}
