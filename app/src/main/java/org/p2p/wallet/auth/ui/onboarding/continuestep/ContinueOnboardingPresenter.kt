package org.p2p.wallet.auth.ui.onboarding.continuestep

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.web3authsdk.UserSignUpDetailsStorage
import org.p2p.wallet.auth.web3authsdk.UserSignUpInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber

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
            try {
                when (val result = userSignUpInteractor.continueSignUpUser()) {
                    UserSignUpInteractor.SignUpResult.SignUpSuccessful -> view?.navigateToPhoneNumberEnter()
                    is UserSignUpInteractor.SignUpResult.SignUpFailed -> Timber.w(result.cause)
                }
            } catch (error: Throwable) {
                Timber.w(error)
            }
        }
    }
}
