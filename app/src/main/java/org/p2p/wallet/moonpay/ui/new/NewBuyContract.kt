package org.p2p.wallet.moonpay.ui.new

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface NewBuyContract {

    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}
