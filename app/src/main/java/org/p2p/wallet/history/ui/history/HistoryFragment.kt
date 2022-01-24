package org.p2p.wallet.history.ui.history

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.common.ui.recycler.EndlessScrollListener
import org.p2p.wallet.databinding.FragmentHistoryBinding
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.ui.details.SwapTransactionFragment
import org.p2p.wallet.history.ui.details.TransferTransactionFragment
import org.p2p.wallet.history.ui.history.adapter.HistoryAdapter
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showErrorDialog
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class HistoryFragment :
    BaseMvpFragment<HistoryContract.View, HistoryContract.Presenter>(R.layout.fragment_history),
    HistoryContract.View {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"

        fun create(token: Token.Active) =
            HistoryFragment().withArgs(EXTRA_TOKEN to token)
    }

    override val presenter: HistoryContract.Presenter by inject {
        parametersOf(token)
    }

    private val token: Token.Active by args(EXTRA_TOKEN)

    private val binding: FragmentHistoryBinding by viewBinding()

    private val historyAdapter: HistoryAdapter by lazy {
        HistoryAdapter(
            onTransactionClicked = { onTransactionClicked(it) },
            onRetryClicked = { presenter.fetchNextPage() }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding.historyRecyclerView) {
            val linearLayoutManager = LinearLayoutManager(requireContext())
            layoutManager = linearLayoutManager
            attachAdapter(historyAdapter)

            val scrollListener = EndlessScrollListener(linearLayoutManager) {
                presenter.fetchNextPage()
            }

            clearOnScrollListeners()
            addOnScrollListener(scrollListener)
        }

        binding.refreshLayout.setOnRefreshListener { presenter.refresh() }

        presenter.loadHistory()
    }

    override fun showError(@StringRes resId: Int, argument: String) {
        showErrorDialog(getString(resId, argument))
    }

    override fun showRefreshing(isRefreshing: Boolean) {
        binding.refreshLayout.isRefreshing = isRefreshing
    }

    override fun showHistory(transactions: List<HistoryTransaction>) {
        historyAdapter.setTransactions(transactions)

        val isEmpty = transactions.isEmpty()
        binding.emptyView.isVisible = isEmpty
        binding.refreshLayout.isVisible = !isEmpty
    }

    override fun showPagingState(newState: PagingState) {
        historyAdapter.setPagingState(newState)
    }

    private fun onTransactionClicked(transaction: HistoryTransaction) {
        when (transaction) {
            is HistoryTransaction.Swap -> replaceFragment(SwapTransactionFragment.create(transaction))
            is HistoryTransaction.Transfer -> replaceFragment(TransferTransactionFragment.create(transaction))
            else -> {
                // todo: add close account and unknown transaction details view
            }
        }
    }
}