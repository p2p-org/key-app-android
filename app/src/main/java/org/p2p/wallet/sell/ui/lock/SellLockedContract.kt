package org.p2p.wallet.sell.ui.lock

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SellLockedContract {

    interface View : MvpView

    interface Presenter : MvpPresenter<View> {
        fun removeFromHistory()
        fun onSendClicked()
        fun onRecipientClicked()
        fun onCopyClicked()
    }
}
