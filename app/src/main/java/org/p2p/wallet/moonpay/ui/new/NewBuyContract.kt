package org.p2p.wallet.moonpay.ui.new

import org.p2p.uikit.components.FocusMode
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.moonpay.model.PaymentMethod

interface NewBuyContract {

    interface View : MvpView {
        fun showTokensToBuy(selectedToken: Token, tokensToBuy: List<Token>)
        fun showCurrency(selectedCurrency: BuyCurrency.Currency)
        fun showPaymentMethods(methods: List<PaymentMethod>)
    }

    interface Presenter : MvpPresenter<View> {
        fun onPaymentMethodSelected(selectedMethod: PaymentMethod)
        fun onSelectTokenClicked()
        fun onSelectCurrencyClicked()
        fun setToken(token: Token)
        fun setCurrency(currency: BuyCurrency.Currency)
        fun setBuyAmount(amount: String, isDelayEnabled: Boolean = true)
        fun onFocusModeChanged(focusMode: FocusMode)
    }
}
