package org.p2p.wallet.auth.ui.restore

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.repository.SignUpFlowDataLocalRepository
import org.p2p.wallet.auth.web3authsdk.UserSignUpInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber

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
            val result = userSignUpInteractor.trySignUpNewUser(idToken, userId)
            if (result == UserSignUpInteractor.SignUpResult.SignUpSuccessful) {
                view?.onSuccessfulSignUp()
            } else {
                Timber.i(result.toString())
            }
        }
    }
}
