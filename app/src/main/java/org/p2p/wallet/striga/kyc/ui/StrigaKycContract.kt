package org.p2p.wallet.striga.kyc.ui

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface StrigaKycContract {
    interface View : MvpView {
        fun navigateBack()
    }

    interface Presenter : MvpPresenter<View>
}
