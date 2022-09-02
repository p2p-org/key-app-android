package org.p2p.wallet.moonpay.ui.new

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
import org.p2p.wallet.moonpay.model.Method
import org.p2p.wallet.moonpay.model.PaymentMethod
import org.p2p.wallet.moonpay.repository.MoonpayRepository
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.Constants

private val TOKENS_VALID_FOR_BUY = setOf(Constants.SOL_SYMBOL, Constants.USDC_SYMBOL)

private val PAYMENT_METHODS = listOf(
    PaymentMethod(
        method = Method.BANK_TRANSFER,
        isSelected = true,
        feePercent = 1f,
        paymentPeriodResId = R.string.buy_period_bank_transfer,
        methodResId = R.string.buy_method_bank_transfer,
        iconResId = R.drawable.ic_bank
    ),
    PaymentMethod(
        method = Method.CARD,
        isSelected = false,
        feePercent = 4.5f,
        paymentPeriodResId = R.string.buy_period_card,
        methodResId = R.string.buy_method_card,
        iconResId = R.drawable.ic_card
    )
)

class NewBuyPresenter(
    private val tokenToBuy: Token,
    private val moonpayRepository: MoonpayRepository,
    private val minBuyErrorFormat: String,
    private val maxBuyErrorFormat: String,
    private val buyAnalytics: BuyAnalytics,
    private val analyticsInteractor: ScreensAnalyticsInteractor,
    private val userInteractor: UserInteractor
) : BasePresenter<NewBuyContract.View>(), NewBuyContract.Presenter {

    override fun attach(view: NewBuyContract.View) {
        super.attach(view)
        loadTokensToBuy()
        view.showPaymentMethods(PAYMENT_METHODS)
    }

    private fun loadTokensToBuy() {
        launch {
            val tokensToBuy = userInteractor.getTokensForBuy(TOKENS_VALID_FOR_BUY.toList())
            view?.initTokensToBuy(tokensToBuy)
        }
    }

    override fun onPaymentMethodSelected(selectedMethod: PaymentMethod) {
        val methods = PAYMENT_METHODS.toMutableList().map { method ->
            method.apply { isSelected = method.method == selectedMethod.method }
        }

        view?.showPaymentMethods(methods)
    }
}
