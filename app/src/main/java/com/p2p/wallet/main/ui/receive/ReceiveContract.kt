package com.p2p.wallet.main.ui.receive

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView

interface ReceiveContract {

    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}