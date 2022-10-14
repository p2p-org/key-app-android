package org.p2p.wallet.solend.ui.aboutearn

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SolendAboutEarnContract {
    interface View : MvpView

    interface Presenter : MvpPresenter<View> {
        fun onNextButtonClicked()
    }
}
