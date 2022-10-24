package org.p2p.wallet.auth.ui.reserveusername

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface ReserveUsernameContract {
    interface View : MvpView {
        fun showUsernameAvailable()
        fun showUsernameNotAvailable()
        fun showUsernameIsChecking()
        fun showUsernameInvalid()
        fun showCreateUsernameFailed()
        fun closeWithSuccess()
    }

    interface Presenter : MvpPresenter<View> {
        fun onUsernameInputChanged(newUsername: String)
        fun onCreateUsernameClicked()
    }
}
