package org.p2p.wallet.striga.ui.countrypicker

import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface StrigaPresetDataPickerContract {

    interface View : MvpView {
        fun showItems(items: List<AnyCellItem>)
        fun updateSearchTitle(titleResId: Int)
        fun setupSearchBar()
    }

    interface Presenter : MvpPresenter<View> {
        fun search(text: String)
    }
}
