package org.p2p.wallet.auth.ui.onboarding

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.FileInteractor
import org.p2p.wallet.auth.interactor.OnboardingInteractor
import org.p2p.wallet.auth.interactor.UserSignUpInteractor
import org.p2p.wallet.auth.interactor.restore.TorusKeyInteractor
import org.p2p.wallet.auth.model.OnboardingFlow
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber

class NewOnboardingPresenter(
    private val userSignUpInteractor: UserSignUpInteractor,
    private val onboardingInteractor: OnboardingInteractor,
    private val torusKeyRestoreInteractor: TorusKeyInteractor,
    private val fileInteractor: FileInteractor
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
            torusKeyRestoreInteractor.obtainTorusKey(idToken, userId)
            when (val result = userSignUpInteractor.trySignUpNewUser()) {
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

    override fun onTermsClick() {
        val file = fileInteractor.getTermsOfUseFile()
        view?.showFile(file)
    }

    override fun onPolicyClick() {
        val file = fileInteractor.getPrivacyPolicyFile()
        view?.showFile(file)
    }
}
