package org.p2p.wallet.history.ui.history

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import timber.log.Timber
import org.p2p.core.glide.GlideManager
import org.p2p.core.token.Token
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.recycler.RoundedDecoration
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.recycler.EndlessScrollListener
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.databinding.FragmentHistoryBinding
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsBottomSheetFragment
import org.p2p.wallet.history.ui.token.adapter.HistoryAdapter
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.moonpay.ui.transaction.SellTransactionDetailsBottomSheet
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding

class HistoryFragment :
    BaseMvpFragment<HistoryContract.View, HistoryContract.Presenter>(R.layout.fragment_history),
    HistoryContract.View {

    companion object {
        fun create() = HistoryFragment()
    }

    override val presenter: HistoryContract.Presenter by inject()
    private val binding: FragmentHistoryBinding by viewBinding()

    private val glideManager: GlideManager by inject()
    private val adapter: HistoryAdapter by unsafeLazy {
        HistoryAdapter(
            glideManager = glideManager,
            onHistoryItemClicked = presenter::onItemClicked,
            onRetryClicked = presenter::loadNextHistoryPage
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.layoutHistoryList) {

            errorStateLayout.buttonRetry.setOnClickListener {
                presenter.refreshHistory()
            }
            val scrollListener = EndlessScrollListener(
                layoutManager = historyRecyclerView.layoutManager as LinearLayoutManager,
                loadNextPage = { presenter.loadNextHistoryPage() }
            )

            historyRecyclerView.addOnScrollListener(scrollListener)
            historyRecyclerView.attachAdapter(adapter)
            historyRecyclerView.addItemDecoration(RoundedDecoration(16f))
            refreshLayout.setOnRefreshListener {
                presenter.refreshHistory()
                scrollListener.reset()
            }

            emptyStateLayout.buttonBuy.setOnClickListener {
                presenter.onBuyClicked()
            }
            emptyStateLayout.buttonReceive.setOnClickListener {
                replaceFragment(ReceiveSolanaFragment.create(token = null))
            }
        }

        listenForSellTransactionDialogDismiss()
        lifecycle.addObserver(presenter)
    }

    private fun listenForSellTransactionDialogDismiss() {
        childFragmentManager.setFragmentResultListener(
            SellTransactionDetailsBottomSheet.REQUEST_KEY_DISMISSED, this
        ) { _, _ -> presenter.loadHistory() }
    }

    override fun showPagingState(state: PagingState) {
        adapter.setPagingState(state)
        with(binding.layoutHistoryList) {
            when (state) {
                is PagingState.InitialLoading -> {
                    shimmerView.root.isVisible = true
                    refreshLayout.isVisible = false
                }
                is PagingState.Idle -> {
                    shimmerView.root.isVisible = false
                    refreshLayout.isVisible = true
                    errorStateLayout.root.isVisible = false
                    emptyStateLayout.root.isVisible = adapter.isEmpty()
                    historyRecyclerView.isVisible = !adapter.isEmpty()
                }
                is PagingState.Loading -> {
                    shimmerView.root.isVisible = adapter.isEmpty()
                    refreshLayout.isVisible = true
                    errorStateLayout.root.isVisible = false
                    emptyStateLayout.root.isVisible = false
                    historyRecyclerView.isVisible = !adapter.isEmpty()
                }
                is PagingState.Error -> {
                    shimmerView.root.isVisible = false
                    refreshLayout.isVisible = true
                    errorStateLayout.root.isVisible = adapter.isEmpty()
                    emptyStateLayout.root.isVisible = false
                    historyRecyclerView.isVisible = !adapter.isEmpty()
                }
            }
        }
    }

    override fun showHistory(history: List<HistoryItem>) {
        with(binding.layoutHistoryList) {
            adapter.setTransactions(history)
            historyRecyclerView.invalidateItemDecorations()

            val isHistoryEmpty = adapter.isEmpty()
            emptyStateLayout.root.isVisible = isHistoryEmpty
            historyRecyclerView.isVisible = !isHistoryEmpty
        }
    }

    override fun showBuyScreen(token: Token) {
        replaceFragment(NewBuyFragment.create(token))
    }

    override fun openTransactionDetailsScreen(transaction: HistoryTransaction) {
        when (transaction) {
            is HistoryTransaction.Swap,
            is HistoryTransaction.Transfer,
            is HistoryTransaction.BurnOrMint -> {
                val state = TransactionDetailsLaunchState.History(transaction)
                HistoryTransactionDetailsBottomSheetFragment.show(
                    fragmentManager = parentFragmentManager,
                    state = state
                )
            }
            else -> {
                Timber.e(IllegalArgumentException("Unsupported transaction type: $transaction"))
            }
        }
    }

    override fun showRefreshing(isRefreshing: Boolean) = with(binding.layoutHistoryList) {
        refreshLayout.isRefreshing = isRefreshing
        refreshLayoutProgressPlaceholder.isVisible = isRefreshing
    }

    override fun scrollToTop() {
        binding.layoutHistoryList.historyRecyclerView.smoothScrollToPosition(0)
    }

    override fun openSellTransactionDetails(sellTransaction: SellTransactionViewDetails) {
        SellTransactionDetailsBottomSheet.show(childFragmentManager, sellTransaction)
    }
}
