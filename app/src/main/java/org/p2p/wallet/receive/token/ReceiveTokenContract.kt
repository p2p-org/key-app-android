package org.p2p.wallet.receive.token

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface ReceiveTokenContract {
    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}