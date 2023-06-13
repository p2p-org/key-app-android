package org.p2p.wallet.settings.ui.devices

import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface DevicesContract {

    interface View : MvpView {
        fun showCells(cells: List<AnyCellItem>)
    }

    interface Presenter : MvpPresenter<View> {
        fun executeDeviceShareChange()
    }
}
