package org.p2p.wallet.home.ui.main.bottomsheet

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface HomeActionsContract {
    interface View : MvpView {
        fun setupHomeActions(isSellFeatureEnabled: Boolean)
    }
    interface Presenter : MvpPresenter<View>
}
