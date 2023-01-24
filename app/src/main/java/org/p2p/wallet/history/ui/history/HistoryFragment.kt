package org.p2p.wallet.history.ui.history

import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.utils.attachAdapter
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.recycler.EndlessScrollListener
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.databinding.FragmentHistoryBinding
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsBottomSheetFragment
import org.p2p.wallet.history.ui.token.adapter.HistoryAdapter
import org.p2p.wallet.moonpay.model.SellTransaction
import org.p2p.wallet.moonpay.ui.transaction.SellTransactionDetailsBottomSheet
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import timber.log.Timber

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
            historyItemMapper = get(),
            onTransactionClicked = presenter::onItemClicked,
            onMoonpayTransactionClicked = presenter::onSellTransactionClicked,
            onRetryClicked = {}
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {

            retryButton.setOnClickListener {
                presenter.refreshHistory()
            }
            val scrollListener = EndlessScrollListener(
                layoutManager = historyRecyclerView.layoutManager as LinearLayoutManager,
                loadNextPage = { presenter.loadNextHistoryPage() }
            )

            historyRecyclerView.addOnScrollListener(scrollListener)
            historyRecyclerView.attachAdapter(adapter)

            refreshLayout.setOnRefreshListener {
                presenter.refreshHistory()
                scrollListener.reset()
            }
            retryButton.setOnClickListener {
                presenter.refreshHistory()
            }
        }

        listenForSellTransactionDialogDismiss()
        presenter.loadHistory()
    }

    private fun listenForSellTransactionDialogDismiss() {
        childFragmentManager.setFragmentResultListener(
            SellTransactionDetailsBottomSheet.REQUEST_KEY_DISMISSED, this
        ) { _, _ -> presenter.loadHistory() }
    }

    override fun onResume() {
        super.onResume()
        // dirty duck-tape to remove hidden transactions from the list
        // when the bottom sheet is closed
        presenter.updateSellTransactions()
    }

    override fun showPagingState(state: PagingState) {
        adapter.setPagingState(state)
        with(binding) {
            shimmerView.root.isVisible = state == PagingState.InitialLoading
            refreshLayout.isVisible = state != PagingState.InitialLoading
            errorStateLayout.isVisible = state is PagingState.Error
            emptyStateLayout.isVisible = state == PagingState.Idle && adapter.isEmpty()
            historyRecyclerView.isVisible =
                (state == PagingState.Idle && !adapter.isEmpty()) || state == PagingState.Loading
        }
    }

    override fun showHistory(
        blockChainTransactions: List<HistoryTransaction>,
        sellTransactions: List<SellTransaction>
    ) {
        adapter.setTransactions(blockChainTransactions, sellTransactions)

        val isHistoryEmpty = adapter.isEmpty()
        binding.emptyStateLayout.isVisible = isHistoryEmpty
        binding.historyRecyclerView.isVisible = !isHistoryEmpty
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

    override fun showRefreshing(isRefreshing: Boolean) = with(binding) {
        refreshLayout.isRefreshing = isRefreshing
        refreshLayoutProgressPlaceholder.isVisible = isRefreshing
    }

    override fun scrollToTop() {
        binding.historyRecyclerView.smoothScrollToPosition(0)
    }

    override fun openSellTransactionDetails(sellTransaction: SellTransactionViewDetails) {
        SellTransactionDetailsBottomSheet.show(childFragmentManager, sellTransaction)
    }
}
