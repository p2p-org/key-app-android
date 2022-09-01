package org.p2p.wallet.moonpay.ui.new

import kotlinx.coroutines.launch
import org.p2p.wallet.common.analytics.interactor.ScreensAnalyticsInteractor
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.moonpay.analytics.BuyAnalytics
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
    private val userInteractor: UserInteractor
) : BasePresenter<NewBuyContract.View>(), NewBuyContract.Presenter {

    override fun attach(view: NewBuyContract.View) {
        super.attach(view)
        loadTokensToBuy()
    }

    private fun loadTokensToBuy() {
        launch {
            val tokensToBuy = userInteractor.getTokensForBuy(TOKENS_VALID_FOR_BUY.toList())
            view?.initTokensToBuy(tokensToBuy)
        }
    }
}
