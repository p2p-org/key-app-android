package org.p2p.wallet.auth.ui.restore

import kotlinx.coroutines.launch
import org.p2p.wallet.auth.interactor.SignUpFlowDataCache
import org.p2p.wallet.auth.web3authsdk.UserSignUpInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import timber.log.Timber

class WalletFoundPresenter(
    private val createAccountUseCase: UserSignUpInteractor,
    private val signUpFlowDataCache: SignUpFlowDataCache
) : BasePresenter<WalletFoundContract.View>(), WalletFoundContract.Presenter {

    override fun attach(view: WalletFoundContract.View) {
        super.attach(view)
        view.setUserId(signUpFlowDataCache.signUpUserId.orEmpty())
    }

    override fun useAnotherGoogleAccount() {
        view?.startGoogleFlow()
    }

    override fun setAlternativeIdToken(userId: String, idToken: String) {
        launch {
            val result = createAccountUseCase.trySignUpNewUser(idToken, userId)
            if (result == UserSignUpInteractor.SignUpResult.SignUpSuccessful) {
                view?.onSuccessfulSignUp()
            } else {
                Timber.i(result.toString())
            }
        }
    }
}
