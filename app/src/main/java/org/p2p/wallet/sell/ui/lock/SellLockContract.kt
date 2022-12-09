package org.p2p.wallet.sell.ui.lock

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SellLockContract {

    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}
