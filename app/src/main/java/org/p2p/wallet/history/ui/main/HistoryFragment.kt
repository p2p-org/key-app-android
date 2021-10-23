package org.p2p.wallet.history.ui.main

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.Entry
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.recycler.EndlessScrollListener
import org.p2p.wallet.common.ui.PagingState
import org.p2p.wallet.databinding.FragmentHistoryBinding
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.ui.details.SwapTransactionFragment
import org.p2p.wallet.history.ui.details.TransferTransactionFragment
import org.p2p.wallet.history.ui.main.adapter.HistoryAdapter
import org.p2p.wallet.main.model.Token
import org.p2p.wallet.main.ui.buy.BuyFragment
import org.p2p.wallet.main.ui.options.TokenOptionsDialog
import org.p2p.wallet.main.ui.receive.ReceiveFragment
import org.p2p.wallet.main.ui.send.SendFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.utils.addFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.attachAdapter
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.showInfoDialog
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs
import org.koin.android.ext.android.inject

class HistoryFragment :
    BaseMvpFragment<HistoryContract.View, HistoryContract.Presenter>(R.layout.fragment_history),
    HistoryContract.View,
    OnHeaderClickListener {

    companion object {
        private const val EXTRA_TOKEN = "EXTRA_TOKEN"

        fun create(token: Token.Active) =
            HistoryFragment().withArgs(
                EXTRA_TOKEN to token
            )
    }

    override val presenter: HistoryContract.Presenter by inject()

    private val token: Token.Active by args(EXTRA_TOKEN)

    private val binding: FragmentHistoryBinding by viewBinding()

    private val historyAdapter: HistoryAdapter by lazy {
        HistoryAdapter(
            onTransactionClicked = { onTransactionClicked(it) },
            onRetryClicked = { presenter.loadHistory(token.publicKey, token.tokenSymbol) },
            listener = this
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            if (!token.isSOL) {
                toolbar.inflateMenu(R.menu.menu_token_details)
                toolbar.setOnMenuItemClickListener {
                    if (it.itemId == R.id.settingsItem) {
                        TokenOptionsDialog.show(childFragmentManager, token)
                        return@setOnMenuItemClickListener true
                    }

                    return@setOnMenuItemClickListener false
                }
            }
            toolbar.title = token.tokenSymbol
            toolbar.subtitle = token.tokenName
            toolbar.setNavigationOnClickListener { popBackStack() }

            buyImageView.setOnClickListener {
                addFragment(BuyFragment.create(token))
            }

            receiveImageView.setOnClickListener {
                addFragment(ReceiveFragment.create(token))
            }

            sendImageView.setOnClickListener {
                addFragment(SendFragment.create(token))
            }

            swapImageView.setOnClickListener {
                addFragment(OrcaSwapFragment.create(token))
            }

            with(historyRecyclerView) {
                val linearLayoutManager = LinearLayoutManager(requireContext())
                layoutManager = linearLayoutManager
                attachAdapter(historyAdapter)

                val scrollListener = EndlessScrollListener(linearLayoutManager) {
                    presenter.loadHistory(token.publicKey, token.tokenSymbol)
                }

                doOnAttach {
                    addOnScrollListener(scrollListener)
                }
                doOnDetach {
                    removeOnScrollListener(scrollListener)
                }
            }
        }

        presenter.loadSolAddress()
        presenter.loadHistory(token.publicKey, token.tokenSymbol)
    }

    override fun showSolAddress(sol: Token.Active) {
        historyAdapter.setHeaderData(token, sol)
    }

    override fun showError(@StringRes resId: Int, argument: String) {
        showInfoDialog(getString(resId, argument))
    }

    override fun showHistory(transactions: List<HistoryTransaction>) {
        historyAdapter.setTransactions(transactions)
    }

    override fun showPagingState(newState: PagingState) {
        historyAdapter.setPagingState(newState)
    }

    override fun showLoading(isLoading: Boolean) {
        binding.progressView.isVisible = isLoading
    }

    override fun showChartData(entries: List<Entry>) {
        historyAdapter.setChartData(entries)
    }

    override fun navigateToFragment(fragment: Fragment) {
        addFragment(fragment)
    }

    override fun loadDailyChartData(tokenSymbol: String, days: Int) {
        presenter.loadDailyChartData(tokenSymbol, days)
    }

    override fun loadHourlyChartData(tokenSymbol: String, hours: Int) {
        presenter.loadHourlyChartData(tokenSymbol, hours)
    }

    private fun onTransactionClicked(transaction: HistoryTransaction) {
        when (transaction) {
            is HistoryTransaction.Swap -> addFragment(SwapTransactionFragment.create(transaction))
            is HistoryTransaction.Transfer -> addFragment(TransferTransactionFragment.create(transaction))
            else -> {
                // todo: add close account and unknown transaction details view
            }
        }
    }
}