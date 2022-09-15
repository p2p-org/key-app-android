package org.p2p.wallet.auth.ui.restore.common

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface CommonRestoreContract {
    interface View : MvpView {
        fun startGoogleFlow()
        fun navigateToPinCreate()
        fun showError(error: String)
        fun onNoTokenFoundError(userId: String)
        fun navigateToPhoneEnter()
        fun setLoadingState(isScreenLoading: Boolean)
    }

    interface Presenter : MvpPresenter<View> {
        fun useGoogleAccount()
        fun useCustomShare()
        fun setGoogleIdToken(userId: String, idToken: String)
        fun switchFlowToRestore()
    }
}
