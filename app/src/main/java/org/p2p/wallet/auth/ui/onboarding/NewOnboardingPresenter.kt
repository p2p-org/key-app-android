package org.p2p.wallet.auth.ui.onboarding

import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.UserSignUpInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.model.OnboardingFlow

class NewOnboardingPresenter(
    private val userSignUpInteractor: UserSignUpInteractor,
    private val onboardingInteractor: OnboardingInteractor
) : BasePresenter<NewOnboardingContract.View>(), NewOnboardingContract.Presenter {

    override fun onSignUpButtonClicked() {
        view?.startGoogleFlow()
    }

    override fun onSignInButtonClicked() {
        view?.startGoogleFlow()
    }

    override fun setIdToken(userId: String, idToken: String) {
        launch {
            view?.setButtonLoadingState(isScreenLoading = true)

            when (val result = userSignUpInteractor.trySignUpNewUser(idToken, userId)) {
                is UserSignUpInteractor.SignUpResult.SignUpSuccessful -> {
                    onboardingInteractor.currentFlow = OnboardingFlow.CreateWallet
                    view?.onSuccessfulSignUp()
                }
                is UserSignUpInteractor.SignUpResult.SignUpFailed -> {
                    Timber.e(result, "Creating new user with device shared failed")
                    view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
                }
                UserSignUpInteractor.SignUpResult.UserAlreadyExists -> {
                    view?.onSameTokenFoundError()
                }
            }
            view?.setButtonLoadingState(isScreenLoading = false)
        }
    }
}
