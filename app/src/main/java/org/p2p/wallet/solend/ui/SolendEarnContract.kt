package org.p2p.wallet.solend.ui

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SolendEarnContract {

    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}
