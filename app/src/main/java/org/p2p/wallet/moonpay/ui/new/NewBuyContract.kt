package org.p2p.wallet.moonpay.ui.new

import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.moonpay.model.PaymentMethod

interface NewBuyContract {

    interface View : MvpView {
        fun initTokensToBuy(tokensToBuy: List<Token>)
        fun showPaymentMethods(methods: List<PaymentMethod>)
    }

    interface Presenter : MvpPresenter<View> {
        fun onPaymentMethodSelected(selectedMethod: PaymentMethod)
    }
}
