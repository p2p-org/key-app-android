package org.p2p.wallet.history.ui.history

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface HistoryContract {
    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}
