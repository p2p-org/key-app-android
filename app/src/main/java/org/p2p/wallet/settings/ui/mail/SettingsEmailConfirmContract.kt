package org.p2p.wallet.settings.ui.mail

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SettingsEmailConfirmContract {
    interface View : MvpView {
        fun startGoogleFlow()
        fun setLoadingState(isScreenLoading: Boolean)
        fun showSuccessScreen()
        fun showErrorScreen()
    }

    interface Presenter : MvpPresenter<View> {
        fun setGoogleIdToken(userId: String, idToken: String)
    }
}