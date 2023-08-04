package org.p2p.wallet.striga.onramp.iban

import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface StrigaIbanImportantNotesContract {
    interface View : MvpView {
        fun showNotes(details: List<AnyCellItem>)
    }

    interface Presenter : MvpPresenter<View> {
        fun onDontShowAgainChecked(isChecked: Boolean)
    }
}
