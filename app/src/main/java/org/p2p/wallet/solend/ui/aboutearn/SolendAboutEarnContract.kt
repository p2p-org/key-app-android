package org.p2p.wallet.solend.ui.aboutearn

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SolendAboutEarnContract {
    interface View : MvpView {
        fun slideNext()
        fun closeOnboarding()
    }

    interface Presenter : MvpPresenter<View> {
        fun onNextButtonClicked()
        fun onContinueButtonClicked()
    }
}
