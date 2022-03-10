package org.p2p.wallet.renbtc.ui.buy

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface RenBtcBuyContract {
    interface View : MvpView

    interface Presenter : MvpPresenter<View> {
        fun onBuyClicked()
    }
}