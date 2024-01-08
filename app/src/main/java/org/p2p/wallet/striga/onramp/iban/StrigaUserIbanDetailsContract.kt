package org.p2p.wallet.striga.onramp.iban

import org.p2p.uikit.components.InformerViewCellModel
import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface StrigaUserIbanDetailsContract {
    interface View : MvpView {
        fun showIbanDetails(details: List<AnyCellItem>)
        fun showImportantNotesInformer(model: InformerViewCellModel)
        fun showImportantNotes()
        fun navigateBack()
    }

    interface Presenter : MvpPresenter<View> {
        fun onImportantNotesInformerClick()
    }
}
