package org.p2p.wallet.sell.ui.information

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SellInformationContract {

    interface View : MvpView {
        fun dismissWithOkResult()
    }

    interface Presenter : MvpPresenter<View> {
        fun onOkClick(notShowAgain: Boolean)
    }
}
