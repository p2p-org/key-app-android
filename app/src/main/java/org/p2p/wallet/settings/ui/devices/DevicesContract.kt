package org.p2p.wallet.settings.ui.devices

import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface DevicesContract {

    interface View : MvpView {
        fun showCells(cells: List<AnyCellItem>)
        fun setLoadingState(isScreenLoading: Boolean)
        fun navigateToSmsInput()
        fun showCommonError()
        fun showSuccessDeviceChange()
        fun showFailDeviceChange()
        fun navigateToAccountBlocked(cooldownTtl: Long)
    }

    interface Presenter : MvpPresenter<View> {
        fun executeDeviceShareChange()
    }
}
