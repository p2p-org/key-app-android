package org.p2p.wallet.auth.ui.restore_error

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.model.RestoreSuccessState
import org.p2p.wallet.auth.repository.RestoreUserExceptionHandler
import org.p2p.wallet.auth.statemachine.RestoreStateMachine
import org.p2p.wallet.common.mvp.BasePresenter

class RestoreErrorScreenPresenter(
    private val restoreFailureState: RestoreFailureState.TitleSubtitleError,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val onboardingInteractor: OnboardingInteractor,
    private val restoreUserExceptionHandler: RestoreUserExceptionHandler,
    private val restoreStateMachine: RestoreStateMachine
) :
    BasePresenter<RestoreErrorScreenContract.View>(),
    RestoreErrorScreenContract.Presenter {

    override fun attach(view: RestoreErrorScreenContract.View) {
        super.attach(view)
        view.showState(restoreFailureState)
    }

    override fun useGoogleAccount() {
        view?.startGoogleFlow()
    }

    override fun setGoogleIdToken(userId: String, idToken: String) {
        launch {
            view?.setLoadingState(isLoading = true)
            restoreWalletInteractor.obtainTorusKey(userId = userId, idToken = idToken)
            onboardingInteractor.currentFlow = restoreStateMachine.getSocialFlow()
            restoreUserWithShares()
            view?.setLoadingState(isLoading = false)
        }
    }

    override fun useCustomShare() {
        onboardingInteractor.currentFlow = restoreStateMachine.getCustomFlow()
        restoreWalletInteractor.resetUserPhoneNumber()
        view?.navigateToPhoneEnter()
    }

    override fun onStartScreenClicked() {
        restoreWalletInteractor.resetUserPhoneNumber()
        view?.navigateToStartScreen()
    }

    private suspend fun restoreUserWithShares() {
        val restoreResult = restoreWalletInteractor.tryRestoreUser(restoreStateMachine.getSocialFlow())
        when (val restoreHandledState = restoreUserExceptionHandler.handleRestoreResult(restoreResult)) {
            is RestoreSuccessState -> {
                restoreWalletInteractor.finishAuthFlow()
                view?.navigateToPinCreate()
            }
            is RestoreFailureState.TitleSubtitleError -> {
                view?.restartWithState(restoreHandledState)
            }
            is RestoreFailureState.ToastError -> {
                view?.showUiKitSnackBar(message = restoreHandledState.message)
            }
        }
    }
}
