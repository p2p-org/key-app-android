package org.p2p.wallet.history.ui.history

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.glide.GlideManager
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.recycler.EndlessScrollListener
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.databinding.FragmentHistoryBinding
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import org.p2p.wallet.history.ui.detailsbottomsheet.TransactionDetailsBottomSheetFragment
import org.p2p.wallet.history.ui.token.adapter.HistoryAdapter
import org.p2p.wallet.utils.WalletLinearLayoutManager
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
            onTransactionClicked = presenter::onItemClicked,
            onRetryClicked = {}
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val layoutManager = WalletLinearLayoutManager(requireContext())
            val scrollListener = EndlessScrollListener(
                layoutManager = layoutManager,
                loadNextPage = { presenter.loadNextHistoryPage() }
            )
            historyRecyclerView.layoutManager = layoutManager
            historyRecyclerView.addOnScrollListener(scrollListener)
            historyRecyclerView.adapter = adapter
            retryButton.setOnClickListener {
                presenter.loadHistory()
            }

            refreshLayout.setOnRefreshListener {
                presenter.refreshHistory()
                scrollListener.reset()
            }
        }
        presenter.loadHistory()
    }

    override fun showPagingState(state: PagingState) {
        adapter.setPagingState(state)
        with(binding) {
            shimmerView.isVisible = state == PagingState.InitialLoading
            refreshLayout.isVisible = state != PagingState.InitialLoading
            errorStateLayout.isVisible = state is PagingState.Error
            historyRecyclerView.isVisible = state == PagingState.Idle || state == PagingState.Loading
        }
    }

    override fun showHistory(items: List<HistoryTransaction>) {
        adapter.setTransactions(items)

        val isHistoryEmpty = items.isEmpty()
        binding.emptyStateLayout.isVisible = isHistoryEmpty
        binding.historyRecyclerView.isVisible = !isHistoryEmpty
    }

    override fun openTransactionDetailsScreen(transaction: HistoryTransaction) {
        when (transaction) {
            is HistoryTransaction.Swap,
            is HistoryTransaction.Transfer,
            is HistoryTransaction.BurnOrMint -> {
                val state = TransactionDetailsLaunchState.History(transaction)
                TransactionDetailsBottomSheetFragment.show(
                    parentFragmentManager, state
                )
            }
            else -> Timber.e("Unsupported transaction type: $transaction")
        }
    }

    override fun showRefreshing(isRefreshing: Boolean) = with(binding) {
        refreshLayout.isRefreshing = isRefreshing
        refreshLayoutProgressPlaceholder.isVisible = isRefreshing
    }

    override fun scrollToTop() {
        binding.historyRecyclerView.smoothScrollToPosition(0)
    }
}
