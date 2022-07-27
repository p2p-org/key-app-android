package org.p2p.wallet.auth.ui.restore

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface WalletFoundContract {
    interface View : MvpView {
        fun startGoogleFlow()
        fun setUserId(userId: String)
        fun showError(error: String)
        fun onSuccessfulSignUp()
    }

    interface Presenter : MvpPresenter<View> {
        fun onSignUpButtonClicked()
        fun setIdToken(userId: String, idToken: String)
    }
}
