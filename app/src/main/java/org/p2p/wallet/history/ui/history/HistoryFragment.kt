package org.p2p.wallet.history.ui.history

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.recycler.EndlessScrollListener
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.databinding.FragmentHistoryBinding
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import org.p2p.wallet.history.ui.token.adapter.HistoryAdapter
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import timber.log.Timber

private const val DISABLED_STATE_ALPHA = 0.3f
private const val ENABLED_STATE_ALPHA = 0.3f

class HistoryFragment :
    BaseMvpFragment<HistoryContract.View, HistoryContract.Presenter>(R.layout.fragment_history),
    HistoryContract.View {

    companion object {
        fun create() = HistoryFragment()
    }

    override val presenter: HistoryContract.Presenter by inject()
    private val binding: FragmentHistoryBinding by viewBinding()

    private var isRefreshing = false
    private val adapter: HistoryAdapter by unsafeLazy {
        HistoryAdapter(
            onTransactionClicked = {
                if (!isRefreshing) {
                    presenter.onItemClicked(it)
                }
            },
            onRetryClicked = {}
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            val scrollListener = EndlessScrollListener(
                layoutManager = historyRecyclerView.layoutManager as LinearLayoutManager,
                loadNextPage = { presenter.loadHistory(false) }
            )

            refreshLayout.setOnRefreshListener {
                presenter.loadHistory(isRefresh = true)
                scrollListener.reset()
            }
            historyRecyclerView.addOnScrollListener(scrollListener)
            historyRecyclerView.adapter = adapter
        }
        presenter.loadHistory()
    }

    override fun showPagingState(state: PagingState) {
        isRefreshing = state is PagingState.Loading && state.isRefresh
        adapter.setPagingState(state)
        with(binding) {
            shimmerView.isVisible = state == PagingState.InitialLoading
            refreshLayout.isRefreshing = state is PagingState.Loading && state.isRefresh
            refreshLayout.isVisible = state != PagingState.InitialLoading
            historyRecyclerView.alpha = if (isRefreshing) {
                DISABLED_STATE_ALPHA
            } else {
                ENABLED_STATE_ALPHA
            }
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
                // TODO PWN-3253 show details here
            }
            else -> Timber.e("Unsupported transaction type: $transaction")
        }
    }
}
