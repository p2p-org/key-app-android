package org.p2p.wallet.auth.ui.restore_error

import org.p2p.wallet.auth.model.RestoreFailureState
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface RestoreErrorScreenContract {
    interface View : MvpView {
        fun showState(state: RestoreFailureState.TitleSubtitleError)
    }

    interface Presenter : MvpPresenter<View> {
        fun useGoogleAccount()
    }
}
