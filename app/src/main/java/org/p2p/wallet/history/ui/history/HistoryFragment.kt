package org.p2p.wallet.history.ui.history

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.recycler.EndlessScrollListener
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.common.ui.widget.ActionButtonsView
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

private const val EXTRA_TOKEN = "EXTRA_TOKEN"

class HistoryFragment :
    BaseMvpFragment<HistoryContract.View, HistoryContract.Presenter>(R.layout.fragment_history),
    HistoryContract.View {

    companion object {
        fun create(tokenForHistory: Token.Active): HistoryFragment =
            HistoryFragment()
                .withArgs(EXTRA_TOKEN to tokenForHistory)
    }

    override val presenter: HistoryContract.Presenter by inject { parametersOf(tokenForHistory) }

    private val tokenForHistory: Token.Active by args(EXTRA_TOKEN)

    private val historyAdapter: HistoryAdapter by unsafeLazy {
        HistoryAdapter(
            onTransactionClicked = presenter::onItemClicked,
            onRetryClicked = presenter::fetchNextPage
        )
    }

    private val binding: FragmentHistoryBinding by viewBinding()

    private val receiveAnalytics: ReceiveAnalytics by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupView()
        presenter.loadHistory()
    }

    private fun FragmentHistoryBinding.setupView() {
        toolbar.setupToolbar()

        totalTextView.text = tokenForHistory.getFormattedTotal(includeSymbol = true)
        usdTotalTextView.text = tokenForHistory.getFormattedUsdTotal()

        refreshLayout.setOnRefreshListener { presenter.refresh() }

        actionButtonsView.setupListeners()

        historyRecyclerView.setupHistoryList()

        appBarLayout.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                val offset = (verticalOffset.toFloat() / appBarLayout.height).absoluteValue
                (actionButtonsView as? OnOffsetChangedListener)?.onOffsetChanged(offset)
            }
        )
    }

    private fun Toolbar.setupToolbar() {
        title = tokenForHistory.tokenName

        setNavigationOnClickListener { popBackStack() }
        if (BuildConfig.DEBUG) {
            inflateMenu(R.menu.menu_history)
            setOnMenuItemClickListener {
                var isHandled = false
                if (it.itemId == R.id.closeItem) {
                    presenter.closeAccount()
                    isHandled = true
                }
                isHandled
            }
        }
    }

    private fun ActionButtonsView.setupListeners() {
        onBuyItemClickListener = {
            replaceFragment(BuySolanaFragment.create(tokenForHistory))
        }
        onReceiveItemClickListener = {
            receiveAnalytics.logTokenReceiveViewed(tokenForHistory.tokenName)
            replaceFragment(ReceiveTokenFragment.create(tokenForHistory))
        }
        onSendClickListener = {
            replaceFragment(SendFragment.create(tokenForHistory))
        }
        onSwapItemClickListener = {
            replaceFragment(OrcaSwapFragment.create(tokenForHistory))
        }
    }

    private fun RecyclerView.setupHistoryList() {
        layoutManager = LinearLayoutManager(requireContext())

        attachAdapter(historyAdapter)
        clearOnScrollListeners()

        val scrollListener = EndlessScrollListener(
            layoutManager = layoutManager as LinearLayoutManager,
            loadNextPage = { presenter.fetchNextPage() },
        )
        addOnScrollListener(scrollListener)
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
        binding.emptyStateLayout.isVisible = isEmpty
        binding.refreshLayout.isVisible = !isEmpty
    }

    override fun showActions(items: List<ActionButton>) {
        binding.actionButtonsView.setItems(items)
    }

    override fun showPagingState(newState: PagingState) {
        historyAdapter.setPagingState(newState)
    }

    override fun openTransactionDetailsScreen(transaction: HistoryTransaction) {
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
