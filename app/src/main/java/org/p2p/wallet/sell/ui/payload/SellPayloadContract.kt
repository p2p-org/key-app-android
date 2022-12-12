package org.p2p.wallet.sell.ui.payload

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SellPayloadContract {

    interface View : MvpView

    interface Presenter : MvpPresenter<View> {
        fun load()
        fun cashOut()
    }
}
