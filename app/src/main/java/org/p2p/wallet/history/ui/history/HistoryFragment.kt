package org.p2p.wallet.history.ui.history

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.recycler.EndlessScrollListener
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.common.ui.widget.ActionButtonsView.ActionButton
import org.p2p.wallet.common.ui.widget.OnOffsetChangedListener
import org.p2p.wallet.databinding.FragmentHistoryBinding
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import org.p2p.wallet.history.ui.details.TransactionDetailsFragment
import org.p2p.wallet.history.ui.history.adapter.HistoryAdapter
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.moonpay.ui.BuySolanaFragment
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.token.ReceiveTokenFragment
import org.p2p.wallet.send.ui.main.SendFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showErrorDialog
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import kotlin.math.absoluteValue

class HistoryFragment :
    BaseMvpFragment<HistoryContract.View, HistoryContract.Presenter>(R.layout.fragment_history),
    HistoryContract.View {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
        fun create(token: Token.Active): HistoryFragment = HistoryFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    override val presenter: HistoryContract.Presenter by inject { parametersOf(token) }

    private val token: Token.Active by args(EXTRA_TOKEN)

    private val historyAdapter: HistoryAdapter by unsafeLazy {
        HistoryAdapter(
            onTransactionClicked = { presenter.onItemClicked(it) },
            onRetryClicked = { presenter.fetchNextPage() }
        )
    }

    private val binding: FragmentHistoryBinding by viewBinding()
    private val receiveAnalytics: ReceiveAnalytics by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.title = token.tokenName
            toolbar.setNavigationOnClickListener { popBackStack() }
            if (BuildConfig.DEBUG) {
                toolbar.inflateMenu(R.menu.menu_history)
                toolbar.setOnMenuItemClickListener {
                    if (it.itemId == R.id.closeItem) {
                        presenter.closeAccount()
                        return@setOnMenuItemClickListener true
                    }

                    return@setOnMenuItemClickListener false
                }
            }

            totalTextView.text = token.getFormattedTotal(includeSymbol = true)
            usdTotalTextView.text = token.getFormattedUsdTotal()
            refreshLayout.setOnRefreshListener { presenter.refresh() }
            with(actionButtonsView) {
                onBuyItemClickListener = {
                    replaceFragment(BuySolanaFragment.create(token))
                }
                onReceiveItemClickListener = {
                    receiveAnalytics.logTokenReceiveViewed(token.tokenName)
                    replaceFragment(ReceiveTokenFragment.create(token))
                }
                onSendClickListener = {
                    replaceFragment(SendFragment.create(token))
                }
                onSwapItemClickListener = {
                    replaceFragment(OrcaSwapFragment.create(token))
                }
            }

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

            appBarLayout.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                    val offset = (verticalOffset.toFloat() / appBarLayout.height).absoluteValue
                    (binding.actionButtonsView as? OnOffsetChangedListener)?.onOffsetChanged(offset)
                }
            )
        }
        presenter.loadHistory()
    }

    override fun showError(@StringRes resId: Int, argument: String) {
        showErrorDialog(getString(resId, argument))
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun showRefreshing(isRefreshing: Boolean) {
        binding.refreshLayout.isRefreshing = isRefreshing
    }

    override fun showHistory(transactions: List<HistoryTransaction>) {
        historyAdapter.setTransactions(transactions)

        val isEmpty = transactions.isEmpty()
        binding.emptyStateLayout.isVisible = isEmpty
        binding.refreshLayout.isVisible = !isEmpty
    }

    override fun showActions(items: List<ActionButton>) {
        binding.actionButtonsView.setItems(items)
    }

    override fun showPagingState(newState: PagingState) {
        historyAdapter.setPagingState(newState)
    }

    override fun showDetails(transaction: HistoryTransaction) {
        when (transaction) {
            is HistoryTransaction.Swap,
            is HistoryTransaction.Transfer,
            is HistoryTransaction.BurnOrMint -> {
                val state = TransactionDetailsLaunchState.History(transaction)
                replaceFragment(TransactionDetailsFragment.create(state))
            }
            else -> {
                // todo: add support of other transactions
            }
        }
    }
}
