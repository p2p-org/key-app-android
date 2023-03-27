package org.p2p.wallet.history.ui.sendvialink

import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface HistorySendLinksContract {
    interface View : MvpView {
        fun showUserLinks(userLinksModels: List<AnyCellItem>)
    }

    interface Presenter : MvpPresenter<View>
}
