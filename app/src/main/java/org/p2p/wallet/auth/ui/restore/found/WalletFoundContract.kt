package org.p2p.wallet.auth.ui.restore.found

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface WalletFoundContract {
    interface View : MvpView {
        fun startGoogleFlow()
        fun setUserId(userId: String)
        fun onSuccessfulSignUp()
        fun onSameTokenFoundError()
        fun setLoadingState(isScreenLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun useAnotherGoogleAccount()
        fun setAlternativeIdToken(userId: String, idToken: String)
    }
}
