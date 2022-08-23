package org.p2p.wallet.auth.ui.onboarding.continuestep

import org.p2p.wallet.R
import org.p2p.wallet.auth.web3authsdk.UserSignUpDetailsStorage
import org.p2p.wallet.auth.web3authsdk.UserSignUpInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber
import kotlinx.coroutines.launch

class ContinueOnboardingPresenter(
    private val userSignUpInteractor: UserSignUpInteractor,
    private val signUpDetailsStorage: UserSignUpDetailsStorage
) : BasePresenter<ContinueOnboardingContract.View>(), ContinueOnboardingContract.Presenter {

    override fun attach(view: ContinueOnboardingContract.View) {
        super.attach(view)
        val userId = signUpDetailsStorage.getLastSignUpUserDetails()?.userId.orEmpty()
        view.showUserId(userId)
    }

    override fun continueSignUp() {
        launch {
            when (val result = userSignUpInteractor.continueSignUpUser()) {
                UserSignUpInteractor.SignUpResult.SignUpSuccessful -> {
                    view?.navigateToPhoneNumberEnter()
                }
                is UserSignUpInteractor.SignUpResult.SignUpFailed -> {
                    Timber.e(result.cause, "Continue sign up failed")
                    view?.showErrorSnackBar(R.string.error_general_message)
                }
            }
        }
    }
}
