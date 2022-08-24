package org.p2p.wallet.auth.ui.restore.common

import org.p2p.wallet.R
import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.UserSignUpInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber

class CommonRestorePresenter(
    private val userSignUpInteractor: UserSignUpInteractor,
) : BasePresenter<CommonRestoreContract.View>(), CommonRestoreContract.Presenter {

    override fun useGoogleAccount() {
        view?.startGoogleFlow()
    }

    override fun setAlternativeIdToken(userId: String, idToken: String) {
        launch {
            // TODO modify logic with userSignInInteractor
            when (val result = userSignUpInteractor.trySignUpNewUser(idToken, userId)) {
                is UserSignUpInteractor.SignUpResult.SignUpSuccessful -> {
                    view?.onSuccessfulSignUp()
                }
                is UserSignUpInteractor.SignUpResult.SignUpFailed -> {
                    Timber.e(result.cause)
                    view?.showErrorSnackBar(R.string.error_general_message)
                }
                UserSignUpInteractor.SignUpResult.UserAlreadyExists -> {
                    view?.onNoTokenFoundError(userId)
                }
            }
        }
    }
}
