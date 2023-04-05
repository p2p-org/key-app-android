package org.p2p.wallet.history.ui.history

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.user.interactor.UserInteractor
import org.p2p.wallet.utils.ifNotEmpty

class HistoryPresenter(
    private val userInteractor: UserInteractor,
) : BasePresenter<HistoryContract.View>(), HistoryContract.Presenter {

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
