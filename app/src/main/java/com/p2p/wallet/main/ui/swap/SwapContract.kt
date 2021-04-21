package com.p2p.wallet.main.ui.swap

import com.p2p.wallet.common.mvp.MvpPresenter
import com.p2p.wallet.common.mvp.MvpView

interface SwapContract {

    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}