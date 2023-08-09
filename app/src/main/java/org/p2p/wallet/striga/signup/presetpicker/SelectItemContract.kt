package org.p2p.wallet.striga.signup.presetpicker

import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SelectItemContract {
    interface View : MvpView {

        fun showItems(items: List<AnyCellItem>)
        fun closeWithResult(selectedItem: SelectableItem)
    }

    interface Presenter : MvpPresenter<View> {
        fun onItemClicked(item: SelectableItem)
        fun search(query: String)
    }
}
