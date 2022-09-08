package org.p2p.wallet.auth.ui.restore.common

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface CommonRestoreContract {
    interface View : MvpView {
        fun startGoogleFlow()
        fun showError(error: String)
        fun onSuccessfulSignUp()
        fun onNoTokenFoundError(userId: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun useGoogleAccount()
        fun setAlternativeIdToken(userId: String, idToken: String)
        fun switchFlowToRestore()
    }
}
