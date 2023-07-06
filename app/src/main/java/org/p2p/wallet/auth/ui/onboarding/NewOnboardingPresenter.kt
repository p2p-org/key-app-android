package org.p2p.wallet.auth.ui.onboarding

import timber.log.Timber
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.FileInteractor
import org.p2p.wallet.auth.interactor.UserSignUpInteractor
import org.p2p.wallet.auth.interactor.restore.TorusKeyInteractor
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.common.mvp.BasePresenter

class NewOnboardingPresenter(
    private val userSignUpInteractor: UserSignUpInteractor,
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
    private val torusKeyRestoreInteractor: TorusKeyInteractor,
    private val fileInteractor: FileInteractor
) : BasePresenter<NewOnboardingContract.View>(), NewOnboardingContract.Presenter {

    override fun onSignUpButtonClicked() {
        if (userSignUpDetailsStorage.isSignUpInProcess()) {
            view?.navigateToContinueCreateWallet()
        } else {
            view?.startGoogleFlow()
        }
    }

    override fun onSignInButtonClicked() {
        view?.startGoogleFlow()
    }

    override fun setIdToken(userId: String, googleIdJwtToken: String) {
        launch {
            Timber.i("Google id token received: idTokenLen=${googleIdJwtToken.length}")
            view?.setButtonLoadingState(isScreenLoading = true)
            torusKeyRestoreInteractor.getTorusKey(googleIdJwtToken = googleIdJwtToken, socialShareUserId = userId)
            when (val result = userSignUpInteractor.trySignUpNewUser(userId)) {
                is UserSignUpInteractor.SignUpResult.SignUpSuccessful -> {
                    view?.onSuccessfulSignUp()
                }
                is UserSignUpInteractor.SignUpResult.SignUpFailed -> {
                    if (result.cause is CancellationException) {
                        Timber.i(result)
                    } else {
                        Timber.e(result, result.cause.message)
                    }
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
