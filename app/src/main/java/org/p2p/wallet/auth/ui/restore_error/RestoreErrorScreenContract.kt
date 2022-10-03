package org.p2p.wallet.auth.ui.restore_error

import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface RestoreErrorScreenContract {
    interface View : MvpView {
        fun showState(state: RestoreFailureState.TitleSubtitleError)
        fun startGoogleFlow()
        fun setLoadingState(isLoading: Boolean)
        fun navigateToPinCreate()
        fun navigateToPhoneEnter()
    }

    interface Presenter : MvpPresenter<View> {
        fun useGoogleAccount()
        fun setGoogleIdToken(userId: String, idToken: String)
        fun useCustomShare()
    }
}
