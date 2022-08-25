package org.p2p.wallet.auth.ui.restore.found

import org.p2p.wallet.R
import org.p2p.wallet.auth.interactor.UserSignUpInteractor
import org.p2p.wallet.auth.repository.SignUpFlowDataLocalRepository
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber
import kotlinx.coroutines.launch

class WalletFoundPresenter(
    private val userSignUpInteractor: UserSignUpInteractor,
    private val signUpFlowDataRepository: SignUpFlowDataLocalRepository
) : BasePresenter<WalletFoundContract.View>(), WalletFoundContract.Presenter {

    override fun attach(view: WalletFoundContract.View) {
        super.attach(view)
        view.setUserId(signUpFlowDataRepository.signUpUserId.orEmpty())
    }

    override fun useAnotherGoogleAccount() {
        view?.startGoogleFlow()
    }

    override fun setAlternativeIdToken(userId: String, idToken: String) {
        launch {
            view?.setLoadingState(isScreenLoading = true)

            when (val result = userSignUpInteractor.trySignUpNewUser(idToken, userId)) {
                UserSignUpInteractor.SignUpResult.SignUpSuccessful -> {
                    view?.onSuccessfulSignUp()
                }
                is UserSignUpInteractor.SignUpResult.SignUpFailed -> {
                    Timber.e(result, "Setting alternative user failed")
                    view?.showUiKitSnackBar(R.string.error_general_message)
                }
                UserSignUpInteractor.SignUpResult.UserAlreadyExists -> {
                    view?.onSameTokenFoundError()
                }
            }
            view?.setLoadingState(isScreenLoading = false)
        }
    }
}
