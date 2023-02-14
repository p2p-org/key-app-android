package org.p2p.wallet.history.ui.historylist

import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.google.android.material.snackbar.Snackbar
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.core.common.TextContainer
import org.p2p.core.glide.GlideManager
import org.p2p.core.token.Token
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.recycler.RoundedDecoration
import org.p2p.wallet.common.ui.recycler.EndlessScrollListener
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.databinding.LayoutHistoryListBinding
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.ui.token.adapter.HistoryAdapter
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails

class HistoryListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), KoinComponent, HistoryListViewContract.View {

    private val binding = inflateViewBinding<LayoutHistoryListBinding>()

    private var tokenForHistory: Token.Active? = null

    private val glideManager: GlideManager by inject()

    private lateinit var historyAdapter: HistoryAdapter

    private lateinit var presenter: HistoryListViewContract.Presenter

    private var onTransactionClickListener: (HistoryTransaction) -> Unit = {}
    private var onSellTransactionClickListener: (SellTransactionViewDetails) -> Unit = {}

    fun bind(
        historyListViewPresenter: HistoryListViewContract.Presenter,
        onTransactionClicked: (HistoryTransaction) -> Unit,
        onSellTransactionClicked: (SellTransactionViewDetails) -> Unit,
        onBuyClicked: () -> Unit,
        onReceiveClicked: () -> Unit,
        token: Token.Active? = null
    ) {
        presenter = historyListViewPresenter
        tokenForHistory = token
        onTransactionClickListener = onTransactionClicked
        onSellTransactionClickListener = onSellTransactionClicked
        historyAdapter = HistoryAdapter(
            glideManager = glideManager,
            onHistoryItemClicked = presenter::onItemClicked,
            onRetryClicked = presenter::loadNextHistoryPage,
        )
        bindView(onBuyClicked, onReceiveClicked)
        presenter.attach(this)
    }

    private fun bindView(
        onBuyClicked: () -> Unit,
        onReceiveClicked: () -> Unit,
    ) {
        with(binding) {
            errorStateLayout.buttonRetry.setOnClickListener {
                presenter.refreshHistory()
            }
            val scrollListener = EndlessScrollListener(
                layoutManager = historyRecyclerView.layoutManager as LinearLayoutManager,
                loadNextPage = { presenter.loadNextHistoryPage() }
            )

            historyRecyclerView.addOnScrollListener(scrollListener)
            historyRecyclerView.attachAdapter(historyAdapter)
            historyRecyclerView.addItemDecoration(RoundedDecoration(16f))
            refreshLayout.setOnRefreshListener {
                presenter.refreshHistory()
                scrollListener.reset()
            }

            emptyStateLayout.buttonBuy.setOnClickListener {
                onBuyClicked.invoke()
            }
            emptyStateLayout.buttonReceive.setOnClickListener {
                onReceiveClicked.invoke()
            }
        }
    }

    override fun onDetachedFromWindow() {
        presenter.detach()
        super.onDetachedFromWindow()
    }

    fun loadHistory() {
        presenter.loadHistory()
    }

    override fun showPagingState(state: PagingState) {
        historyAdapter.setPagingState(state)
        val isHistoryEmpty = historyAdapter.isEmpty()
        with(binding) {
            when (state) {
                is PagingState.InitialLoading -> {
                    shimmerView.root.isVisible = true
                    refreshLayout.isVisible = false
                }
                is PagingState.Idle -> {
                    shimmerView.root.isVisible = false
                    refreshLayout.isVisible = true
                    errorStateLayout.root.isVisible = false
                    emptyStateLayout.root.isVisible = isHistoryEmpty
                    historyRecyclerView.isVisible = !isHistoryEmpty
                }
                is PagingState.Loading -> {
                    shimmerView.root.isVisible = isHistoryEmpty
                    refreshLayout.isVisible = true
                    errorStateLayout.root.isVisible = false
                    emptyStateLayout.root.isVisible = false
                    historyRecyclerView.isVisible = !isHistoryEmpty
                }
                is PagingState.Error -> {
                    shimmerView.root.isVisible = false
                    refreshLayout.isVisible = true
                    errorStateLayout.root.isVisible = isHistoryEmpty
                    emptyStateLayout.root.isVisible = false
                    historyRecyclerView.isVisible = !isHistoryEmpty
                }
            }
        }
    }

    override fun showHistory(history: List<HistoryItem>) {
        with(binding) {
            historyAdapter.setTransactions(history)
            historyRecyclerView.invalidateItemDecorations()

            val isHistoryEmpty = historyAdapter.isEmpty()
            emptyStateLayout.root.isVisible = isHistoryEmpty
            historyRecyclerView.isVisible = !isHistoryEmpty
        }
    }

    override fun onTransactionClicked(transaction: HistoryTransaction) {
        onTransactionClickListener(transaction)
    }

    override fun onSellTransactionClicked(sellTransactionDetails: SellTransactionViewDetails) {
        onSellTransactionClickListener(sellTransactionDetails)
    }

    override fun showRefreshing(isRefreshing: Boolean) = with(binding) {
        refreshLayout.isRefreshing = isRefreshing
        refreshLayoutProgressPlaceholder.isVisible = isRefreshing
    }

    override fun scrollToTop() {
        binding.historyRecyclerView.smoothScrollToPosition(0)
    }

    fun addObserver(lifecycle: Lifecycle) {
        lifecycle.addObserver(presenter)
    }

    //region Not Needed Base Methods
    override fun showErrorMessage(e: Throwable?) = Unit
    override fun showErrorMessage(messageResId: Int) = Unit
    override fun showErrorSnackBar(message: String, actionResId: Int?, block: (() -> Unit)?) = Unit
    override fun showErrorSnackBar(messageResId: Int, actionResId: Int?, block: (() -> Unit)?) = Unit
    override fun showErrorSnackBar(e: Throwable, actionResId: Int?, block: (() -> Unit)?) = Unit
    override fun showSuccessSnackBar(message: String, actionResId: Int?, block: (() -> Unit)?) = Unit
    override fun showSuccessSnackBar(messageResId: Int, actionResId: Int?, block: (() -> Unit)?) = Unit
    override fun showInfoSnackBar(message: String, iconResId: Int?, actionResId: Int?, actionBlock: (() -> Unit)?) =
        Unit

    override fun showToast(message: TextContainer) = Unit
    override fun showUiKitSnackBar(
        message: String?,
        messageResId: Int?,
        onDismissed: () -> Unit,
        actionButtonResId: Int?,
        actionBlock: ((Snackbar) -> Unit)?
    ) = Unit
    //endregion
}
