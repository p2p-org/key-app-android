package org.p2p.wallet.auth.ui.onboarding

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.web3authsdk.UserSignUpInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber

class NewOnboardingPresenter(
    private val createAccountUseCase: UserSignUpInteractor
) : BasePresenter<NewOnboardingContract.View>(), NewOnboardingContract.Presenter {

    override fun onSignUpButtonClicked() {
        view?.startGoogleFlow()
    }

    override fun onSignInButtonClicked() {
        view?.startGoogleFlow()
    }

    override fun setIdToken(userId: String, idToken: String) {
        launch {
            when (val result = createAccountUseCase.trySignUpNewUser(idToken, userId)) {
                is UserSignUpInteractor.SignUpResult.SignUpSuccessful -> {
                    view?.onSuccessfulSignUp()
                }
                is UserSignUpInteractor.SignUpResult.SignUpFailed -> {
                    Timber.e(result.cause)
                    result.message?.let { view?.showError(it) }
                }
                UserSignUpInteractor.SignUpResult.UserAlreadyExists -> {
                    view?.onSameTokenFoundError()
                }
            }
        }
    }
}
