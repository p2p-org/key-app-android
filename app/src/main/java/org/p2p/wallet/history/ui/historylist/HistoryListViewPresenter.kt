package org.p2p.wallet.history.ui.historylist

import timber.log.Timber
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.p2p.wallet.common.feature_toggles.toggles.remote.SendViaLinkFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryPagingResult
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.ui.model.HistoryItem
import org.p2p.wallet.infrastructure.network.environment.NetworkEnvironmentManager
import org.p2p.wallet.infrastructure.sendvialink.UserSendLinksLocalRepository
import org.p2p.wallet.sell.interactor.HistoryItemMapper

private const val PAGE_SIZE = 20
private const val NO_LINKS_AVAILABLE_VALUE = 0

class HistoryListViewPresenter(
    private val historyInteractor: HistoryInteractor,
    private val environmentManager: NetworkEnvironmentManager,
    private val historyItemMapper: HistoryItemMapper,
    private val userSendLinksRepository: UserSendLinksLocalRepository,
    private val sendViaLinksToggle: SendViaLinkFeatureToggle
) : BasePresenter<HistoryListViewContract.View>(), HistoryListViewContract.Presenter {

    override fun attach(view: HistoryListViewContract.View) {
        super.attach(view)
        attachToHistoryFlow()
    }

    private fun attachToHistoryFlow() {
        launch {
            historyItemMapper.getHistoryAdapterItemFlow()
                .filterNotNull()
                .collect { items ->
                    view?.showHistory(items)
                    view?.showPagingState(PagingState.Idle)
                }
        }
    }

    override fun attach(historyType: HistoryListViewType) {
        environmentManager.addEnvironmentListener(this::class) {
            refreshHistory(historyType)
        }
    }

    override fun loadNextHistoryPage(historyType: HistoryListViewType) {
        launch {
            try {
                view?.showPagingState(PagingState.Loading)
                val result = historyInteractor.loadNextPage(
                    limit = PAGE_SIZE,
                    mintAddress = historyType.mintAddress.base58Value
                )
                val newHistoryTransactions = handlePagingResult(result)
                historyItemMapper.toAdapterItem(
                    transactions = newHistoryTransactions,
                    userSendLinksCount = getUserSendLinksCount(historyType),
                )
            } catch (e: Throwable) {
                Timber.e("Error on loading next history page: $e")
                view?.showPagingState(PagingState.Error(e))
            }
        }
    }

    override fun loadHistory(historyType: HistoryListViewType) {
        launch {
            try {
                view?.showPagingState(PagingState.InitialLoading)
                val result = historyInteractor.loadHistory(
                    limit = PAGE_SIZE,
                    mintAddress = historyType.mintAddress.base58Value
                )
                val newHistoryTransactions = handlePagingResult(result)
                historyItemMapper.toAdapterItem(
                    transactions = newHistoryTransactions,
                    userSendLinksCount = getUserSendLinksCount(historyType),
                )
            } catch (e: Throwable) {
                Timber.e(e, "Error on loading history: $e")
                view?.showPagingState(PagingState.Error(e))
            }
        }
    }

    override fun refreshHistory(historyType: HistoryListViewType) {
        launch {
            try {
                view?.showRefreshing(isRefreshing = true)
                val result = historyInteractor.loadHistory(
                    limit = PAGE_SIZE,
                    mintAddress = historyType.mintAddress.base58Value
                )
                val newHistoryTransactions = handlePagingResult(result)
                historyItemMapper.toAdapterItem(
                    transactions = newHistoryTransactions,
                    userSendLinksCount = getUserSendLinksCount(historyType),
                )
            } catch (e: Throwable) {
                Timber.e(e, "Error on loading history: $e")
                view?.showPagingState(PagingState.Error(e))
            } finally {
                view?.showRefreshing(isRefreshing = false)
            }
        }
    }

    override fun onItemClicked(historyItem: HistoryItem) {
        launch {
            when (historyItem) {
                is HistoryItem.TransactionItem -> {
                    view?.onTransactionClicked(historyItem.transactionId)
                }
                is HistoryItem.MoonpayTransactionItem -> {
                    view?.onSellTransactionClicked(historyItem.transactionId)
                }
                is HistoryItem.UserSendLinksItem -> {
                    view?.onUserSendLinksClicked()
                }
                else -> {
                    val errorMessage = "Unsupported Transaction click! $historyItem"
                    Timber.e(UnsupportedOperationException(errorMessage))
                }
            }
        }
    }

    private suspend fun getUserSendLinksCount(historyType: HistoryListViewType): Int {
        return if (sendViaLinksToggle.isFeatureEnabled && historyType is HistoryListViewType.AllHistory) {
            userSendLinksRepository.getUserLinksCount()
        } else {
            NO_LINKS_AVAILABLE_VALUE
        }
    }

    @Throws(HistoryPagingResult.Error::class)
    private fun handlePagingResult(result: HistoryPagingResult): List<HistoryTransaction> {
        return when (result) {
            is HistoryPagingResult.Error -> throw result
            is HistoryPagingResult.Success -> result.data
        }
    }
}
