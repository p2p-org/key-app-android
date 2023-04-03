package org.p2p.wallet.history.ui.history

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.analytics.HistoryAnalytics
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.ifNotEmpty

class HistoryPresenter(
    private val userInteractor: UserInteractor,
    private val historyAnalytics: HistoryAnalytics,
) : BasePresenter<HistoryContract.View>(), HistoryContract.Presenter {

    override fun attach(view: HistoryContract.View) {
        super.attach(view)
        historyAnalytics.onScreenOpened()
    }

    override fun onBuyClicked() {
        launch {
            userInteractor.getTokensForBuy().ifNotEmpty {
                view?.showBuyScreen(it.first())
            }
        }
    }

    override fun onTransactionClicked(transactionId: String) {
        view?.openTransactionDetailsScreen(transactionId)
    }

    override fun onSellTransactionClicked(transactionId: String) {
        view?.openSellTransactionDetails(transactionId)
    }
}
