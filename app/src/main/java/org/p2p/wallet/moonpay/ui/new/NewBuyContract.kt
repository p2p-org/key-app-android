package org.p2p.wallet.moonpay.ui.new

import org.p2p.uikit.components.FocusField
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.core.token.Token
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.moonpay.model.BuyDetailsState
import org.p2p.wallet.moonpay.model.BuyViewData
import org.p2p.wallet.moonpay.model.PaymentMethod

interface NewBuyContract {

    interface View : MvpView {
        fun showPreselectedAmount(amount: String)
        fun showTokensToBuy(selectedToken: Token, tokensToBuy: List<Token>)
        fun showCurrency(currencies: List<BuyCurrency.Currency>, selectedCurrency: BuyCurrency.Currency)
        fun setCurrencyCode(selectedCurrencyCode: String)
        fun showPaymentMethods(methods: List<PaymentMethod>?)
        fun setContinueButtonEnabled(isEnabled: Boolean)
        fun showLoading(isLoading: Boolean)
        fun showMessage(message: String?, selectedTokenSymbol: String? = null)
        fun showTotal(viewData: BuyViewData)
        fun showDetailsBottomSheet(buyDetailsState: BuyDetailsState)
        fun setCurrencySelectionIsEnabled(enabled: Boolean)
        fun clearOppositeFieldAndTotal(totalText: String)
        fun navigateToMoonpay(
            amount: String,
            selectedToken: Token,
            selectedCurrency: BuyCurrency.Currency,
            paymentMethod: String?
        )

        fun close()
    }

    interface Presenter : MvpPresenter<View> {
        fun onPaymentMethodSelected(selectedMethod: PaymentMethod, byUser: Boolean = true)
        fun onSelectTokenClicked()
        fun onSelectCurrencyClicked()
        fun onTotalClicked()
        fun setTokenToBuy(token: Token)
        fun setCurrency(currency: BuyCurrency.Currency, byUser: Boolean = true)
        fun setBuyAmount(amount: String, isDelayEnabled: Boolean = true)
        fun onFocusFieldChanged(focusField: FocusField)
        fun onContinueClicked()
        fun onBackPressed()
    }
}
