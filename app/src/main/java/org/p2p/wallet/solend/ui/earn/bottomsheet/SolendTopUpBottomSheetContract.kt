package org.p2p.wallet.solend.ui.earn.bottomsheet

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.core.token.Token

interface SolendTopUpBottomSheetContract {

    interface View : MvpView {
        fun showBuyScreen(token: Token)
        fun showReceiveScreen(token: Token)
    }

    interface Presenter : MvpPresenter<View> {
        fun onBuyClicked()
        fun onReceiveClicked()
    }
}
