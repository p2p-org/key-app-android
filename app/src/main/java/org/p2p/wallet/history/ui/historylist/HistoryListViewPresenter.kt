package org.p2p.wallet.history.ui.historylist

import timber.log.Timber
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import org.p2p.wallet.common.feature_toggles.toggles.remote.SendViaLinkFeatureToggle
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.analytics.HistoryAnalytics
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
    private val sendViaLinksToggle: SendViaLinkFeatureToggle,
    private val historyAnalytics: HistoryAnalytics,
) : BasePresenter<HistoryListViewContract.View>(), HistoryListViewContract.Presenter {

    private var isInitialHistoryLoaded = false

    override fun attach(view: HistoryListViewContract.View) {
        super.attach(view)
        launch {
            // stub HistoryListViewType.AllHistory because we check if user has saved links or not
            val isBlockExists = getUserSendLinksCount(HistoryListViewType.AllHistory) != NO_LINKS_AVAILABLE_VALUE
            historyAnalytics.onScreenOpened(isSendViaLinkBlockVisible = isBlockExists)
        }
        attachToHistoryFlow()
    }

    private fun attachToHistoryFlow() {
        launch {
            historyItemMapper.getHistoryAdapterItemFlow()
                .filterNotNull()
                .distinctUntilChanged()
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
                    tokenMintAddress = historyType.mintAddress,
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
        if (isInitialHistoryLoaded) return

        launch {
            try {
                view?.showPagingState(PagingState.InitialLoading)
                val result = historyInteractor.loadHistory(
                    limit = PAGE_SIZE,
                    mintAddress = historyType.mintAddress.base58Value
                )
                val newHistoryTransactions = handlePagingResult(result)

                historyItemMapper.toAdapterItem(
                    tokenMintAddress = historyType.mintAddress,
                    transactions = newHistoryTransactions,
                    userSendLinksCount = getUserSendLinksCount(historyType),
                )
                isInitialHistoryLoaded = true
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
                    tokenMintAddress = historyType.mintAddress,
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
                    historyAnalytics.logUserSendLinksBlockClicked()
                    view?.onUserSendLinksClicked()
                }
                is HistoryItem.SwapBannerItem -> {
                    view?.onSwapBannerItemClicked(
                        sourceTokenMint = historyItem.sourceTokenMintAddress,
                        destinationTokenMint = historyItem.destinationTokenMintAddress,
                        sourceSymbol = historyItem.sourceTokenSymbol,
                        destinationSymbol = historyItem.destinationTokenSymbol,
                        openedFrom = historyItem.openedFrom
                    )
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
