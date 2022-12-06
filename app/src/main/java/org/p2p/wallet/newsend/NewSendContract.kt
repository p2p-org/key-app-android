package org.p2p.wallet.newsend

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface NewSendContract {
    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}
