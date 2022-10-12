package org.p2p.wallet.solend.ui.withdraw

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SolendWithdrawContract {

    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}
