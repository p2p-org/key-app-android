package org.p2p.wallet.history.ui.new_history

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface NewHistoryContract {

    interface View : MvpView

    interface Presenter : MvpPresenter<View> {
        fun loadHistory()
    }
}
