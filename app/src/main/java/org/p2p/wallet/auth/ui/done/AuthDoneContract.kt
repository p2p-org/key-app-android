package org.p2p.wallet.auth.ui.done

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface AuthDoneContract {
    interface View : MvpView {
        fun showUsername(name: String?)
    }

    interface Presenter : MvpPresenter<View> {
        fun load()
    }
}
