package org.p2p.wallet.jupiter.ui.settings

import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.utils.text.TextViewCellModel
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.jupiter.ui.info.SwapInfoType

interface JupiterSwapSettingsContract {
    interface View : MvpView {
        fun bindSettingsList(list: List<AnyCellItem>)
        fun setRatioState(state: TextViewCellModel?)
        fun showDetailsDialog(type: SwapInfoType)
        fun showRouteDialog()
    }

    interface Presenter : MvpPresenter<View> {
        fun onSettingItemClick(item: MainCellModel)
        fun onCustomSlippageChange(slippage: Double?)
    }
}
