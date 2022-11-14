package org.p2p.wallet.auth.ui.generalerror

import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.restore.RestoreWalletInteractor
import org.p2p.wallet.auth.model.GatewayHandledState
import org.p2p.wallet.auth.model.RestoreError
import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.auth.model.RestoreSuccessState
import org.p2p.wallet.auth.repository.RestoreUserResultHandler
import org.p2p.wallet.auth.statemachine.RestoreStateMachine
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber
import kotlinx.coroutines.launch

class OnboardingGeneralErrorPresenter(
    private val gatewayHandledState: GatewayHandledState,
    private val restoreWalletInteractor: RestoreWalletInteractor,
    private val onboardingInteractor: OnboardingInteractor,
    private val restoreStateMachine: RestoreStateMachine,
    private val restoreErrorHandler: RestoreUserResultHandler
) : BasePresenter<OnboardingGeneralErrorContract.View>(),
    OnboardingGeneralErrorContract.Presenter {

    override fun attach(view: OnboardingGeneralErrorContract.View) {
        super.attach(view)
        when (gatewayHandledState) {
            is GatewayHandledState.TitleSubtitleError -> {
                view.setState(gatewayHandledState)
            }
            is GatewayHandledState.CriticalError -> {
                view.setState(gatewayHandledState)
            }
            else -> {
                // Do nothing
            }
        }
    }

    override fun onEnterPhoneClicked() {
        restoreWalletInteractor.resetUserPhoneNumber()
        view?.navigateToEnterPhone()
    }

    override fun onStartScreenClicked() {
        restoreWalletInteractor.resetUserPhoneNumber()
        view?.navigateToStartScreen()
    }

    override fun onGoogleAuthClicked() {
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

    private suspend fun restoreUserWithShares() {
        val restoreResult = restoreWalletInteractor.tryRestoreUser(restoreStateMachine.getSocialFlow())
        when (val restoreHandledState = restoreErrorHandler.handleRestoreResult(restoreResult)) {
            is RestoreSuccessState -> {
                restoreWalletInteractor.finishAuthFlow()
                view?.navigateToPinCreate()
            }
            is RestoreFailureState.TitleSubtitleError -> {
                view?.restartWithState(restoreHandledState)
            }
            is RestoreFailureState.ToastError -> {
                Timber.i(RestoreError(restoreHandledState.message))
                view?.showUiKitSnackBar(message = restoreHandledState.message)
            }
            is RestoreFailureState.LogError -> {
                Timber.e(RestoreError(restoreHandledState.message))
            }
        }
    }
}
