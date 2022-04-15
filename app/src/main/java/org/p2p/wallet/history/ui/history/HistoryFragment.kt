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
import org.p2p.wallet.history.ui.details.TransactionDetailsFragment
import org.p2p.wallet.history.ui.token.adapter.HistoryAdapter
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.replaceFragment
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
    private val adapter: HistoryAdapter by unsafeLazy {
        HistoryAdapter(
            onTransactionClicked = presenter::onItemClicked,
            onRetryClicked = {}
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.tag("________").d("onViewCreate")
        with(binding) {
            // refreshLayout.setOnRefreshListener { presenter.loadHistory(isRefresh = true) }

            val scrollListener = EndlessScrollListener(
                layoutManager = historyRecyclerView.layoutManager as LinearLayoutManager,
                loadNextPage = { presenter.loadHistory(false) }
            )
            historyRecyclerView.addOnScrollListener(scrollListener)
            historyRecyclerView.adapter = adapter
        }
        presenter.loadHistory()
    }

    override fun showPagingState(state: PagingState) {
        adapter.setPagingState(state)
        binding.shimmerView.isVisible = state == PagingState.InitialLoading
    }

    override fun showHistory(items: List<HistoryTransaction>) {
        Timber.tag("_____").d("Loaded items = ${items.size}")
        adapter.setTransactions(items)

        val isEmpty = items.isEmpty()
        binding.emptyStateLayout.isVisible = isEmpty
        binding.historyRecyclerView.isVisible = !isEmpty
    }

    override fun openTransactionDetailsScreen(transaction: HistoryTransaction) {
        when (transaction) {
            is HistoryTransaction.Swap,
            is HistoryTransaction.Transfer,
            is HistoryTransaction.BurnOrMint -> {
                val state = TransactionDetailsLaunchState.History(transaction)
                //TODO PWN-3253 show details here
            }
            else -> Timber.e("Unsupported transaction type: $transaction")
        }
    }
}
