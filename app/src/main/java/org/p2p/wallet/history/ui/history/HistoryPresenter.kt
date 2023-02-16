package org.p2p.wallet.history.ui.history

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.analytics.HistoryAnalytics
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.rpc.RpcHistoryTransaction
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.user.interactor.UserInteractor

class HistoryPresenter(
    private val userInteractor: UserInteractor,
    private val historyAnalytics: HistoryAnalytics,
    private val renBtcInteractor: RenBtcInteractor,
) : BasePresenter<HistoryContract.View>(), HistoryContract.Presenter {

    override fun onBuyClicked() {
        launch {
            val tokensForBuy = userInteractor.getTokensForBuy()
            if (tokensForBuy.isEmpty()) return@launch
            view?.showBuyScreen(tokensForBuy.first())
        }
    }

    override fun onTransactionClicked(transaction: HistoryTransaction) {
        logTransactionClicked(transaction)
        view?.openTransactionDetailsScreen(transaction)
    }

    override fun onSellTransactionClicked(sellTransactionDetails: SellTransactionViewDetails) {
        historyAnalytics.logSellTransactionClicked(sellTransactionDetails)
        view?.openSellTransactionDetails(sellTransactionDetails)
    }

    private fun logTransactionClicked(transaction: HistoryTransaction) {
        when (transaction) {
            is RpcHistoryTransaction.Swap -> {
                historyAnalytics.logSwapTransactionClicked(transaction)
            }
            is RpcHistoryTransaction.Transfer -> {
                launch {
                    historyAnalytics.logTransferTransactionClicked(
                        transaction = transaction,
                        isRenBtcSessionActive = renBtcInteractor.isUserHasActiveSession()
                    )
                }
            }
            else -> Unit
        }
    }
}
