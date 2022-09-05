package org.p2p.wallet.moonpay.ui.new

import org.p2p.uikit.components.FocusMode
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.moonpay.model.BuyViewData
import org.p2p.wallet.moonpay.model.PaymentMethod

interface NewBuyContract {

    interface View : MvpView {
        fun showTokensToBuy(selectedToken: Token, tokensToBuy: List<Token>)
        fun showCurrency(selectedCurrency: BuyCurrency.Currency)
        fun showPaymentMethods(methods: List<PaymentMethod>)
        fun setContinueButtonEnabled(isEnabled: Boolean)
        fun showLoading(isLoading: Boolean)
        fun showMessage(message: String?)
        fun showTotal(viewData: BuyViewData)
        fun showTotalData(viewData: BuyViewData)
        fun navigateToMoonpay(amount: String, selectedToken: Token, selectedCurrency: BuyCurrency.Currency)
        fun close()
    }

    interface Presenter : MvpPresenter<View> {
        fun onPaymentMethodSelected(selectedMethod: PaymentMethod)
        fun onSelectTokenClicked()
        fun onSelectCurrencyClicked()
        fun onTotalClicked()
        fun setToken(token: Token)
        fun setCurrency(currency: BuyCurrency.Currency)
        fun setBuyAmount(amount: String, isDelayEnabled: Boolean = true)
        fun onFocusModeChanged(focusMode: FocusMode)
        fun onContinueClicked()
        fun onBackPressed()
    }
}
