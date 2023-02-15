package org.p2p.wallet.history.ui.historylist

import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.launch
import timber.log.Timber
import org.p2p.core.token.Token
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.interactor.HistoryServiceInteractor
import org.p2p.wallet.history.ui.model.HistoryItem
import org.p2p.wallet.history.ui.history.HistorySellTransactionMapper
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.infrastructure.sell.HiddenSellTransactionsStorageContract
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.sell.interactor.HistoryItemMapper

class HistoryListViewPresenter(
    private val token: Token.Active?,
    private val historyInteractor: HistoryServiceInteractor,
    private val hiddenSellTransactionsStorage: HiddenSellTransactionsStorageContract,
    private val environmentManager: NetworkEnvironmentManager,
    private val sellTransactionsMapper: HistorySellTransactionMapper,
    private val historyItemMapper: HistoryItemMapper,
) : BasePresenter<HistoryListViewContract.View>(), HistoryListViewContract.Presenter {

    override fun attach(view: HistoryListViewContract.View) {
        super.attach(view)
        environmentManager.addEnvironmentListener(this::class) { refreshHistory() }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        loadHistory()
    }

    override fun loadNextHistoryPage() = Unit

    override fun loadHistory() {
        launch {
            try {
                val items = historyInteractor.loadHistory(20, 0)
                val adapterItems = historyItemMapper.toAdapterItem(items)
                view?.showHistory(adapterItems)
                view?.showPagingState(PagingState.Idle)
            } catch (e: Throwable) {
                Timber.tag("______").e(e)
            }
        }
    }

    override fun refreshHistory() = Unit

    override fun onItemClicked(historyItem: HistoryItem) {
        when (historyItem) {
            is HistoryItem.TransactionItem -> {
                val item = historyInteractor.findTransactionBySignature(historyItem.signature) ?: error(
                    "Transaction not founded for history item! $historyItem"
                )
                view?.onTransactionClicked(item)
            }
            is HistoryItem.MoonpayTransactionItem -> {
                val item = historyInteractor.findTransactionBySignature(historyItem.transactionId) ?: error(
                    "Transaction not founded for history item! $historyItem"
                )
                view?.onSellTransactionClicked(historyItemMapper.sellTransactionToDetails(item as SellTransaction))
            }
            else -> {
                val errorMessage = "Unsupported Transaction click! $historyItem"
                Timber.e(errorMessage)
                throw UnsupportedOperationException(errorMessage)
            }
        }
    }
}
