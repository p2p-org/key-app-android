package org.p2p.wallet.history.ui.info

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.recycler.EndlessScrollListener
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.common.ui.widget.ActionButtonsView.ActionButton
import org.p2p.wallet.databinding.FragmentTokenInfoBinding
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.ui.details.TransactionDetailsFragment
import org.p2p.wallet.history.ui.history.adapter.HistoryAdapter
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.receive.solana.ReceiveSolanaFragment
import org.p2p.wallet.send.ui.SendFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showErrorDialog
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

class TokenInfoFragment :
    BaseMvpFragment<TokenInfoContract.View, TokenInfoContract.Presenter>(R.layout.fragment_token_info),
    TokenInfoContract.View {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"
        fun create(token: Token.Active) = TokenInfoFragment().withArgs(
            EXTRA_TOKEN to token
        )
    }

    override val presenter: TokenInfoContract.Presenter by inject {
        parametersOf(token)
    }
    private val token: Token.Active by args(EXTRA_TOKEN)
    private val historyAdapter: HistoryAdapter by lazy {
        HistoryAdapter(
            onTransactionClicked = { onTransactionClicked(it) },
            onRetryClicked = { presenter.fetchNextPage() }
        )
    }

    private val binding: FragmentTokenInfoBinding by viewBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            toolbar.title = token.tokenName
            toolbar.setNavigationOnClickListener { popBackStack() }
            totalTextView.text = token.getFormattedTotal()
            usdTotalTextView.text = token.getFormattedUsdTotal()
            refreshLayout.setOnRefreshListener { presenter.refresh() }
            with(actionButtonsView) {
                onBuyItemClickListener = {
                    // TODO open buy screen
                }
                onReceiveItemClickListener = {
                    replaceFragment(ReceiveSolanaFragment.create(token))
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
        binding.emptyView.isVisible = isEmpty
        binding.refreshLayout.isVisible = !isEmpty
    }

    override fun showActions(items: List<ActionButton>) {
        binding.actionButtonsView.setItems(items)
    }

    override fun showPagingState(newState: PagingState) {
        historyAdapter.setPagingState(newState)
    }

    private fun onTransactionClicked(transaction: HistoryTransaction) {
        replaceFragment(TransactionDetailsFragment.create(transaction))
    }
}