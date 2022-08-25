package org.p2p.wallet.auth.ui.onboarding

import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.UserSignUpInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber
import kotlinx.coroutines.launch

class NewOnboardingPresenter(
    private val userSignUpInteractor: UserSignUpInteractor
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
                    view?.onSuccessfulSignUp()
                }
                is UserSignUpInteractor.SignUpResult.SignUpFailed -> {
                    Timber.e(result, "Creating new user with device shared failed")
                    view?.showUiKitSnackBar(R.string.error_general_message)
                }
                UserSignUpInteractor.SignUpResult.UserAlreadyExists -> {
                    view?.onSameTokenFoundError()
                }
            }
            view?.setButtonLoadingState(isScreenLoading = false)
        }
    }
}
