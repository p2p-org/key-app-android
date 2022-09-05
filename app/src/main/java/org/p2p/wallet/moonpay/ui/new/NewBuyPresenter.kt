package org.p2p.wallet.moonpay.ui.new

import kotlinx.coroutines.launch
import org.p2p.uikit.components.FocusMode
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.home.ui.select.bottomsheet.SelectCurrencyBottomSheet
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.moonpay.interactor.PaymentMethodsInteractor
import org.p2p.wallet.moonpay.model.BuyCurrency
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.moonpay.repository.MoonpayRepository
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.Constants

private val TOKENS_VALID_FOR_BUY = setOf(Constants.SOL_SYMBOL, Constants.USDC_SYMBOL)

class NewBuyPresenter(
    private val tokenToBuy: Token,
    private val moonpayRepository: MoonpayRepository,
    private val minBuyErrorFormat: String,
    private val maxBuyErrorFormat: String,
    private val buyAnalytics: BuyAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    private val userInteractor: UserInteractor,
    private val paymentMethodsInteractor: PaymentMethodsInteractor
) : BasePresenter<NewBuyContract.View>(), NewBuyContract.Presenter {

    private val paymentMethods = mutableListOf<PaymentMethod>()

    private lateinit var tokensToBuy: List<Token>
    private var selectedCurrency: BuyCurrency.Currency = SelectCurrencyBottomSheet.DEFAULT_CURRENCY
    private var selectedToken: Token = tokenToBuy
    private var isSwappedToToken: Boolean = false

    override fun attach(view: NewBuyContract.View) {
        super.attach(view)
        loadTokensToBuy()
        loadAvailablePaymentMethods()
    }

    private fun loadTokensToBuy() {
        launch {
            tokensToBuy = userInteractor.getTokensForBuy(TOKENS_VALID_FOR_BUY.toList())
            selectedToken = tokensToBuy.find { it.tokenSymbol == Constants.USDC_SYMBOL } ?: tokenToBuy
        }
    }

    private fun loadAvailablePaymentMethods() {
        launch {
            // show loading
            paymentMethods.addAll(paymentMethodsInteractor.getAvailablePaymentMethods())
            view?.showPaymentMethods(paymentMethods)
        }
    }

    override fun onPaymentMethodSelected(selectedMethod: PaymentMethod) {
        paymentMethods.forEach { paymentMethod ->
            paymentMethod.isSelected = paymentMethod.method == selectedMethod.method
        }

        view?.showPaymentMethods(paymentMethods)
    }

    override fun onSelectTokenClicked() {
        view?.showTokensToBuy(selectedToken, tokensToBuy)
    }

    override fun onSelectCurrencyClicked() {
        view?.showCurrency(selectedCurrency)
    }

    override fun setToken(token: Token) {
        selectedToken = token
    }

    override fun setCurrency(currency: BuyCurrency.Currency) {
        selectedCurrency = currency
    }

    override fun setBuyAmount(amount: String, isDelayEnabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun onFocusModeChanged(focusMode: FocusMode) {
        isSwappedToToken = focusMode == FocusMode.CURRENCY
    }
}
