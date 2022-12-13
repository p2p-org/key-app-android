package org.p2p.wallet.moonpay.ui.transaction

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface BuyTransactionDetailsContract {
    interface View : MvpView

    interface Presenter : MvpPresenter<View>
}
