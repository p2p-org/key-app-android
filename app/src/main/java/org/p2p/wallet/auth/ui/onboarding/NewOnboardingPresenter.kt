package org.p2p.wallet.auth.ui.onboarding

import timber.log.Timber
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.p2p.core.network.environment.NetworkEnvironmentManager.Companion.URL_PRIVACY_POLICY
import org.p2p.core.network.environment.NetworkEnvironmentManager.Companion.URL_TERMS_OF_USE
import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.UserSignUpInteractor
import org.p2p.wallet.auth.interactor.restore.TorusKeyInteractor
import org.p2p.wallet.auth.repository.UserSignUpDetailsStorage
import org.p2p.wallet.common.mvp.BasePresenter

class NewOnboardingPresenter(
    private val userSignUpInteractor: UserSignUpInteractor,
    private val userSignUpDetailsStorage: UserSignUpDetailsStorage,
    private val torusKeyRestoreInteractor: TorusKeyInteractor
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
        view?.showBrowserTab(URL_TERMS_OF_USE)
    }

    override fun onPolicyClick() {
        view?.showBrowserTab(URL_PRIVACY_POLICY)
    }
}
