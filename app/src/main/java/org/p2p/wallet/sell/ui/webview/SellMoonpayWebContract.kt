package org.p2p.wallet.sell.ui.webview

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SellMoonpayWebContract {
    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}
