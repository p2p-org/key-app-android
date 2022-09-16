package org.p2p.wallet.auth.ui.onboarding.continuestep

import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.UserSignUpInteractor
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.model.OnboardingFlow

class ContinueOnboardingPresenter(
    private val userSignUpInteractor: UserSignUpInteractor,
    private val signUpDetailsStorage: UserSignUpDetailsStorage,
    private val onboardingInteractor: OnboardingInteractor
) : BasePresenter<ContinueOnboardingContract.View>(), ContinueOnboardingContract.Presenter {

    override fun attach(view: ContinueOnboardingContract.View) {
        super.attach(view)
        val userId = signUpDetailsStorage.getLastSignUpUserDetails()?.userId.orEmpty()
        view.showUserId(userId)
    }

    override fun continueSignUp() {
        launch {
            view?.setLoadingState(isScreenLoading = true)

            when (val result = userSignUpInteractor.continueSignUpUser()) {
                UserSignUpInteractor.SignUpResult.SignUpSuccessful -> {
                    onboardingInteractor.currentFlow = OnboardingFlow.CreateWallet
                    view?.navigateToPhoneNumberEnter()
                }
                is UserSignUpInteractor.SignUpResult.SignUpFailed -> {
                    Timber.e(result, "Continue sign up failed")
                    view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
                }
                else -> {
                    view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
                }
            }

            view?.setLoadingState(isScreenLoading = false)
        }
    }
}
