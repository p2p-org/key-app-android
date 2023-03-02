package org.p2p.wallet.swap.ui.jupiter.settings

import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface JupiterSwapSettingsContract {
    interface View : MvpView {
        fun bindSettingsList(list: List<AnyCellItem>)
    }

    interface Presenter : MvpPresenter<View> {
        fun onSettingItemClick(item: FinanceBlockCellModel)
    }
}
