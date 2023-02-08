package org.p2p.wallet.auth.ui.restore.common

import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.auth.model.RestoreError
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.model.RestoreSuccessState
import org.p2p.wallet.auth.repository.RestoreUserResultHandler
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.auth.statemachine.RestoreState
import org.p2p.wallet.auth.statemachine.RestoreStateMachine
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber
import kotlinx.coroutines.launch

class CommonRestorePresenter(
    private val onboardingInteractor: OnboardingInteractor,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val accountStorageContract: UserSignUpDetailsStorage,
    private val restoreUserResultHandler: RestoreUserResultHandler,
    private val restoreStateMachine: RestoreStateMachine
) : BasePresenter<CommonRestoreContract.View>(), CommonRestoreContract.Presenter {

    override fun useGoogleAccount() {
        view?.startGoogleFlow()
    }

    override fun useCustomShare() {
        onboardingInteractor.currentFlow = restoreStateMachine.getCustomFlow()
        restoreWalletInteractor.resetUserPhoneNumber()
        view?.navigateToPhoneEnter()
    }

    override fun useSeedPhrase() {
        onboardingInteractor.currentFlow = OnboardingFlow.RestoreWallet()
    }

    override fun switchFlowToRestore() {
        launch {
            val isDeviceShareSaved = restoreWalletInteractor.isDeviceShareSaved()
            restoreStateMachine.updateState(RestoreState.CommonRestoreScreenState(isDeviceShareSaved))
            restoreWalletInteractor.generateRestoreUserKeyPair()
            view?.setRestoreViaGoogleFlowVisibility(
                isVisible = accountStorageContract.isDeviceShareSaved() && !accountStorageContract.isSignUpInProcess()
            )
        }
    }

    override fun setGoogleIdToken(userId: String, idToken: String) {
        launch {
            view?.setLoadingState(isScreenLoading = true)
            onboardingInteractor.currentFlow = restoreStateMachine.getSocialFlow()
            restoreWalletInteractor.obtainTorusKey(userId = userId, idToken = idToken)
            restoreUserWithShares(onboardingInteractor.currentFlow as OnboardingFlow.RestoreWallet)
            view?.setLoadingState(isScreenLoading = false)
        }
    }

    private suspend fun restoreUserWithShares(currentFlow: OnboardingFlow.RestoreWallet) {
        onboardingInteractor.currentFlow = currentFlow
        val restoreResult = restoreWalletInteractor.tryRestoreUser(currentFlow)
        when (val restoreHandledState = restoreUserResultHandler.handleRestoreResult(restoreResult)) {
            is RestoreSuccessState -> {
                restoreWalletInteractor.finishAuthFlow()
                view?.navigateToPinCreate()
            }
            is RestoreFailureState.TitleSubtitleError -> {
                view?.showRestoreErrorScreen(restoreHandledState)
                restoreUserWithShares(currentFlow)
            }
            is RestoreFailureState.ToastError -> {
                view?.showUiKitSnackBar(message = restoreHandledState.message)
            }
            is RestoreFailureState.LogError -> {
                Timber.e(RestoreError(restoreHandledState.message))
            }
        }
    }
}
