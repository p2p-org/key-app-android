package org.p2p.wallet.history.ui.historylist

import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.launch
import timber.log.Timber
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.ui.model.HistoryItem
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.sell.interactor.HistoryItemMapper

private const val PAGE_SIZE = 20
class HistoryListViewPresenter(
    private val historyInteractor: HistoryInteractor,
    private val environmentManager: NetworkEnvironmentManager,
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
                view?.showPagingState(PagingState.InitialLoading)
                val items = historyInteractor.loadHistory(PAGE_SIZE)
                val adapterItems = historyItemMapper.toAdapterItem(items)
                view?.showHistory(adapterItems)
                view?.showPagingState(PagingState.Idle)
            } catch (e: Throwable) {
                Timber.e("Error on loading history: $e")
                view?.showPagingState(PagingState.Error(e))
            }
        }
    }

    override fun refreshHistory() = Unit

    override fun onItemClicked(historyItem: HistoryItem) {
        when (historyItem) {
            is HistoryItem.TransactionItem -> {
                val item = historyInteractor.findTransactionById(historyItem.signature) ?: error(
                    "Transaction not founded for history item! $historyItem"
                )
                view?.onTransactionClicked(item)
            }
            is HistoryItem.MoonpayTransactionItem -> {
                val item = historyInteractor.findTransactionById(historyItem.transactionId) ?: error(
                    "Transaction not founded for history item! $historyItem"
                )
                val adapterItem = historyItemMapper.toAdapterItem(item as SellTransaction)
                view?.onSellTransactionClicked(adapterItem)
            }
            else -> {
                val errorMessage = "Unsupported Transaction click! $historyItem"
                Timber.e(errorMessage)
                throw UnsupportedOperationException(errorMessage)
            }
        }
    }
}
