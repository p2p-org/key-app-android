package org.p2p.wallet.striga.presetpicker

import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.striga.presetpicker.interactor.StrigaPresetDataItem

interface StrigaPresetDataPickerContract {

    interface View : MvpView {
        fun showItems(items: List<AnyCellItem>)
        fun closeWithResult(selectedItem: StrigaPresetDataItem)
    }

    interface Presenter : MvpPresenter<View> {
        fun search(text: String)
        fun onPresetDataSelected(item: StrigaPresetDataItem)
    }
}
