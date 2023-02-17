package org.p2p.wallet.history.ui.history

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.analytics.HistoryAnalytics
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.user.interactor.UserInteractor

class HistoryPresenter(
    private val userInteractor: UserInteractor,
    private val historyAnalytics: HistoryAnalytics
) : BasePresenter<HistoryContract.View>(), HistoryContract.Presenter {

    override fun onBuyClicked() {
        launch {
            val tokensForBuy = userInteractor.getTokensForBuy()
            if (tokensForBuy.isEmpty()) return@launch
            view?.showBuyScreen(tokensForBuy.first())
        }
    }

    override fun onTransactionClicked(transactionId: String) {
        view?.openTransactionDetailsScreen(transactionId)
    }

    override fun onSellTransactionClicked(sellTransactionDetails: SellTransactionViewDetails) {
        historyAnalytics.logSellTransactionClicked(sellTransactionDetails)
        view?.openSellTransactionDetails(sellTransactionDetails)
    }
}
