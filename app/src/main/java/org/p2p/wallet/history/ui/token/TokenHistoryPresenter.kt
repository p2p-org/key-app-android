package org.p2p.wallet.history.ui.token

import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.history.analytics.HistoryAnalytics
import org.p2p.wallet.history.interactor.HistoryInteractor
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.infrastructure.sell.HiddenSellTransactionsStorageContract
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.renbtc.interactor.RenBtcInteractor
import org.p2p.wallet.rpc.interactor.TokenInteractor
import timber.log.Timber
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class TokenHistoryPresenter(
    private val token: Token.Active,
    private val historyInteractor: HistoryInteractor,
    private val hiddenSellTransactionsStorage: HiddenSellTransactionsStorageContract,
    private val historyAnalytics: HistoryAnalytics,
    private val renBtcInteractor: RenBtcInteractor,
    private val tokenInteractor: TokenInteractor,
) : BasePresenter<TokenHistoryContract.View>(), TokenHistoryContract.Presenter {

    private class FetchListResult<T>(
        content: MutableList<T> = mutableListOf(),
        val isFailed: Boolean = false,
    ) {
        private val innerContent = content

        private var contentFilter: ((T) -> Boolean)? = null

        val content: List<T>
            get() = contentFilter?.let(innerContent::filter) ?: innerContent

        fun withContentFilter(filter: (T) -> Boolean): FetchListResult<T> = apply {
            contentFilter = filter
        }

        fun hasItems(): Boolean = content.isNotEmpty()

        fun clearContent() {
            innerContent.clear()
        }
    }

    private object HistoryFetchFailure : Throwable(message = "Both transactions were not fetch due to errors")

    private var blockChainTransactionsList = FetchListResult<HistoryTransaction>(mutableListOf())
    private var sellTransactionsList = FetchListResult<SellTransaction>(mutableListOf())

    private var pagingJob: Job? = null

    private var refreshJob: Job? = null
    private var paginationEnded: Boolean = false

    override fun attach(view: TokenHistoryContract.View) {
        super.attach(view)
        initialize()
    }

    private fun initialize() {
        val actionButtons = mutableListOf(
            ActionButton.RECEIVE_BUTTON,
            ActionButton.SEND_BUTTON,
            ActionButton.SWAP_BUTTON
        )

        if (token.isSOL || token.isUSDC) {
            actionButtons.add(0, ActionButton.BUY_BUTTON)
        }

        view?.showActionButtons(actionButtons)
    }

    override fun loadNextHistoryPage() {
        if (paginationEnded) return

        if (pagingJob?.isActive == true) {
            return
        }
        pagingJob = launch {
            view?.showPagingState(PagingState.Loading)
            fetchHistory()
        }
    }

    override fun retryLoad() {
        paginationEnded = false
        refreshJob?.cancel()

        refreshJob = launch {
            view?.showRefreshing(isRefreshing = true)
            fetchHistory(isRefresh = true)
            view?.showRefreshing(isRefreshing = false)
            view?.scrollToTop()
        }
    }

    override fun loadHistory() {
        if (blockChainTransactionsList.hasItems() || sellTransactionsList.hasItems()) {
            view?.showHistory(blockChainTransactionsList.content, sellTransactionsList.content)
            return
        }
        launch {
            view?.showPagingState(PagingState.InitialLoading)
            fetchHistory()
        }
    }

    private suspend fun fetchHistory(isRefresh: Boolean = false) {
        try {
            if (isRefresh) {
                blockChainTransactionsList.clearContent()
                sellTransactionsList.clearContent()
            }
            if (token.isSOL) {
                sellTransactionsList = fetchSellTransactions()
            }
            blockChainTransactionsList = fetchBlockChainTransactions(isRefresh)
            if (blockChainTransactionsList.isFailed && sellTransactionsList.isFailed) {
                view?.showPagingState(PagingState.Error(HistoryFetchFailure))
                Timber.e(HistoryFetchFailure, "Error getting transaction history for token")
            } else {
                view?.showHistory(blockChainTransactionsList.content, sellTransactionsList.content)
                view?.showPagingState(PagingState.Idle)
            }
        } catch (e: CancellationException) {
            Timber.i(e, "Cancelled history next page load")
        }
    }

    private suspend fun fetchBlockChainTransactions(isRefresh: Boolean) = try {
        historyInteractor.loadTransactions(isRefresh)
            .toMutableList()
            .let(::FetchListResult)
    } catch (error: Throwable) {
        Timber.e(error, "Error while loading blockchain transactions on history")
        FetchListResult(isFailed = true)
    }

    private suspend fun fetchSellTransactions(): FetchListResult<SellTransaction> = try {
        historyInteractor.getSellTransactions()
            .toMutableList()
            .let(::FetchListResult)
            .withContentFilter { !hiddenSellTransactionsStorage.isTransactionHidden(it.transactionId) }
    } catch (error: Throwable) {
        Timber.e(error, "Error while loading Moonpay sell transactions on history")
        FetchListResult(isFailed = true)
    }

    override fun onItemClicked(transaction: HistoryTransaction) {
        logTransactionClicked(transaction)
        view?.showDetailsScreen(transaction)
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
            else -> Unit // log other types later
        }
    }

    override fun closeAccount() {
        launch {
            try {
                tokenInteractor.closeTokenAccount(token.publicKey)
                view?.showUiKitSnackBar(messageResId = R.string.details_account_closed_successfully)
            } catch (e: Throwable) {
                Timber.e(e, "Error closing account: ${token.publicKey}")
                view?.showErrorMessage(e)
            }
        }
    }
}
