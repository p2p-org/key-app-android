package org.p2p.wallet.history.ui.history

import timber.log.Timber
import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.history.analytics.HistoryAnalytics
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
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

    override fun onItemClicked(historyItem: HistoryItem) {
        when (historyItem) {
            is HistoryItem.TransactionItem -> onTransactionItemClicked(historyItem.transaction)
            is HistoryItem.MoonpayTransactionItem -> onSellTransactionClicked(historyItem.transactionDetails)
            else -> {
                val errorMessage = "Unsupported Transaction click! $historyItem"
                Timber.e(errorMessage)
                throw UnsupportedOperationException(errorMessage)
            }
        }
    }

    private fun onTransactionItemClicked(transaction: HistoryTransaction) {
        logTransactionClicked(transaction)
        view?.openTransactionDetailsScreen(transaction)
    }

    private fun logTransactionClicked(transaction: HistoryTransaction) {
        when (transaction) {
            is HistoryTransaction.Swap -> {
                historyAnalytics.logSwapTransactionClicked(transaction)
            }
            is HistoryTransaction.Transfer -> {
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

    private fun onSellTransactionClicked(sellTransaction: SellTransactionViewDetails) {
        historyAnalytics.logSellTransactionClicked(sellTransaction)
        view?.openSellTransactionDetails(sellTransaction)
    }
}
