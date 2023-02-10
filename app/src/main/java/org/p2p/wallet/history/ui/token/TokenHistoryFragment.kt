package org.p2p.wallet.history.ui.token

import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.os.Bundle
import android.view.View
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber
import org.p2p.core.glide.GlideManager
import org.p2p.core.token.Token
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.recycler.RoundedDecoration
import org.p2p.wallet.BuildConfig
import org.p2p.wallet.R
import org.p2p.wallet.common.feature_toggles.toggles.remote.NewBuyFeatureToggle
import org.p2p.wallet.common.mvp.BaseMvpFragment
import org.p2p.wallet.common.ui.recycler.EndlessScrollListener
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.common.ui.widget.actionbuttons.ActionButton
import org.p2p.wallet.databinding.FragmentTokenHistoryBinding
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.model.TransactionDetailsLaunchState
import org.p2p.wallet.history.ui.detailsbottomsheet.HistoryTransactionDetailsBottomSheetFragment
import org.p2p.wallet.history.ui.token.adapter.HistoryAdapter
import org.p2p.wallet.moonpay.ui.BuySolanaFragment
import org.p2p.wallet.moonpay.ui.new.NewBuyFragment
import org.p2p.wallet.moonpay.ui.transaction.SellTransactionDetailsBottomSheet
import org.p2p.wallet.newsend.ui.search.NewSearchFragment
import org.p2p.wallet.receive.analytics.ReceiveAnalytics
import org.p2p.wallet.receive.token.ReceiveTokenFragment
import org.p2p.wallet.sell.ui.payload.SellPayloadFragment
import org.p2p.wallet.swap.ui.orca.OrcaSwapFragment
import org.p2p.wallet.utils.args
import org.p2p.wallet.utils.popBackStack
import org.p2p.wallet.utils.replaceFragment
import org.p2p.wallet.utils.showErrorDialog
import org.p2p.wallet.utils.unsafeLazy
import org.p2p.wallet.utils.viewbinding.viewBinding
import org.p2p.wallet.utils.withArgs

private const val EXTRA_TOKEN = "EXTRA_TOKEN"

class TokenHistoryFragment :
    BaseMvpFragment<TokenHistoryContract.View, TokenHistoryContract.Presenter>(R.layout.fragment_token_history),
    TokenHistoryContract.View {

    companion object {
        fun create(tokenForHistory: Token.Active): TokenHistoryFragment =
            TokenHistoryFragment()
                .withArgs(EXTRA_TOKEN to tokenForHistory)
    }

    override val presenter: TokenHistoryContract.Presenter by inject { parametersOf(tokenForHistory) }

    private val tokenForHistory: Token.Active by args(EXTRA_TOKEN)

    private val glideManager: GlideManager by inject()

    private val historyAdapter: HistoryAdapter by unsafeLazy {
        HistoryAdapter(
            glideManager = glideManager,
            onTransactionClicked = presenter::onItemClicked,
            onRetryClicked = presenter::loadNextHistoryPage,
            onMoonpayTransactionClicked = { SellTransactionDetailsBottomSheet.show(childFragmentManager, it) }
        )
    }

    private val binding: FragmentTokenHistoryBinding by viewBinding()

    private val receiveAnalytics: ReceiveAnalytics by inject()

    private val newBuyFeatureToggle: NewBuyFeatureToggle by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setupView()
        listenForSellTransactionDialogDismiss()
        lifecycle.addObserver(presenter)
    }

    private fun listenForSellTransactionDialogDismiss() {
        childFragmentManager.setFragmentResultListener(
            SellTransactionDetailsBottomSheet.REQUEST_KEY_DISMISSED, this
        ) { _, _ -> presenter.loadHistory() }
    }

    private fun FragmentTokenHistoryBinding.setupView() {
        toolbar.setupToolbar()

        totalTextView.text = tokenForHistory.getFormattedTotal(includeSymbol = true)
        usdTotalTextView.text = tokenForHistory.getFormattedUsdTotal()
        viewActionButtons.onButtonClicked = { onActionButtonClicked(it) }
        with(layoutHistoryList) {
            refreshLayout.setOnRefreshListener { presenter.retryLoad() }
            errorStateLayout.buttonRetry.setOnClickListener { presenter.retryLoad() }
            historyRecyclerView.setupHistoryList()

            emptyStateLayout.buttonBuy.setOnClickListener { onActionButtonClicked(ActionButton.BUY_BUTTON) }
            emptyStateLayout.buttonReceive.setOnClickListener { onActionButtonClicked(ActionButton.RECEIVE_BUTTON) }
        }
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

    private fun onActionButtonClicked(clickedButton: ActionButton) {
        when (clickedButton) {
            ActionButton.BUY_BUTTON -> {
                replaceFragment(
                    if (newBuyFeatureToggle.value) {
                        NewBuyFragment.create(tokenForHistory)
                    } else {
                        BuySolanaFragment.create(tokenForHistory)
                    }
                )
            }
            ActionButton.RECEIVE_BUTTON -> {
                receiveAnalytics.logTokenReceiveViewed(tokenForHistory.tokenName)
                replaceFragment(ReceiveTokenFragment.create(tokenForHistory))
            }
            ActionButton.SEND_BUTTON -> {
                replaceFragment(NewSearchFragment.create(tokenForHistory))
            }
            ActionButton.SWAP_BUTTON -> {
                replaceFragment(OrcaSwapFragment.create(tokenForHistory))
            }
            ActionButton.SELL_BUTTON -> {
                replaceFragment(SellPayloadFragment.create())
            }
        }
    }

    private fun RecyclerView.setupHistoryList() {
        layoutManager = LinearLayoutManager(requireContext())

        attachAdapter(historyAdapter)
        addItemDecoration(RoundedDecoration(16f))
        clearOnScrollListeners()

        val scrollListener = EndlessScrollListener(
            layoutManager = layoutManager as LinearLayoutManager,
            loadNextPage = { presenter.loadNextHistoryPage() },
        )
        addOnScrollListener(scrollListener)
    }

    override fun showError(@StringRes resId: Int, argument: String) {
        showErrorDialog(getString(resId, argument))
    }

    override fun showRefreshing(isRefreshing: Boolean) {
        binding.layoutHistoryList.refreshLayout.isRefreshing = isRefreshing
    }

    override fun showHistory(history: List<HistoryItem>) {
        with(binding.layoutHistoryList) {
            historyAdapter.setTransactions(history)
            historyRecyclerView.invalidateItemDecorations()

            val isEmpty = historyAdapter.isEmpty()
            emptyStateLayout.root.isVisible = isEmpty
            refreshLayout.isVisible = !isEmpty
        }
    }

    override fun showActionButtons(actionButtons: List<ActionButton>) {
        binding.viewActionButtons.showActionButtons(actionButtons)
    }

    override fun showPagingState(newState: PagingState) {
        Timber.tag("_____STATE").d(newState.toString())
        historyAdapter.setPagingState(newState)
        with(binding.layoutHistoryList) {
            shimmerView.root.isVisible = newState == PagingState.InitialLoading
            refreshLayout.isVisible = newState != PagingState.InitialLoading
            errorStateLayout.root.isVisible = newState is PagingState.Error
            emptyStateLayout.root.isVisible = newState == PagingState.Idle && historyAdapter.isEmpty()
            historyRecyclerView.isVisible = recyclerVisibilityValidState(newState)
        }
    }

    private fun recyclerVisibilityValidState(state: PagingState): Boolean {
        val isInitState = state == PagingState.Idle && !historyAdapter.isEmpty()
        val isFetchPageErrorState = state is PagingState.Error && !historyAdapter.isEmpty()
        return isInitState || isFetchPageErrorState || state == PagingState.Loading
    }

    override fun showDetailsScreen(transaction: HistoryTransaction) {
        when (transaction) {
            is HistoryTransaction.Swap,
            is HistoryTransaction.Transfer,
            is HistoryTransaction.BurnOrMint -> {
                val state = TransactionDetailsLaunchState.History(transaction)
                HistoryTransactionDetailsBottomSheetFragment.show(parentFragmentManager, state)
            }
            else -> Timber.e("Unsupported transactionType: $transaction")
        }
    }

    override fun scrollToTop() {
        with(binding.layoutHistoryList.historyRecyclerView) {
            post {
                smoothScrollToPosition(0)
            }
        }
    }
}
