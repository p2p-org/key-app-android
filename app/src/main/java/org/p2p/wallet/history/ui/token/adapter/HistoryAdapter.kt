package org.p2p.wallet.history.ui.token.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.utils.recycler.RoundedItem
import org.p2p.uikit.utils.recycler.RoundedItemAdapterInterface
import org.p2p.wallet.common.date.isSameAs
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.ui.model.HistoryItem
import org.p2p.wallet.history.ui.model.HistoryItem.DateItem
import org.p2p.wallet.history.ui.model.HistoryItem.MoonpayTransactionItem
import org.p2p.wallet.history.ui.model.HistoryItem.TransactionItem
import org.p2p.wallet.history.ui.token.adapter.holders.DateViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.ErrorViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.HistorySellTransactionViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.HistoryTransactionViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.ProgressViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.TransactionSwapViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.TransactionViewHolder

private const val TRANSACTION_VIEW_TYPE = 1
private const val HISTORY_DATE_VIEW_TYPE = 2
private const val PROGRESS_VIEW_TYPE = 3
private const val ERROR_VIEW_TYPE = 4
private const val TRANSACTION_SWAP_VIEW_TYPE = 5
private const val TRANSACTION_MOONPAY_VIEW_TYPE = 6

class HistoryAdapter(
    private val glideManager: GlideManager,
    private val onHistoryItemClicked: (HistoryItem) -> Unit,
    private val onRetryClicked: () -> Unit,
) : ListAdapter<HistoryItem, HistoryTransactionViewHolder>(HistoryItemComparataor), RoundedItemAdapterInterface {

    private val pagingController = HistoryAdapterPagingController(this)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryTransactionViewHolder {
        return when (viewType) {
            TRANSACTION_VIEW_TYPE -> TransactionViewHolder(parent, glideManager, onHistoryItemClicked)
            TRANSACTION_SWAP_VIEW_TYPE -> TransactionSwapViewHolder(parent, glideManager, onHistoryItemClicked)
            HISTORY_DATE_VIEW_TYPE -> DateViewHolder(parent)
            PROGRESS_VIEW_TYPE -> ProgressViewHolder(parent)
            TRANSACTION_MOONPAY_VIEW_TYPE -> HistorySellTransactionViewHolder(parent, onHistoryItemClicked)
            else -> ErrorViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: HistoryTransactionViewHolder, position: Int) {
        when (holder) {
            is TransactionViewHolder -> holder.onBind(getItem(position) as TransactionItem)
            is TransactionSwapViewHolder -> holder.onBind(getItem(position) as TransactionItem)
            is DateViewHolder -> holder.onBind(getItem(position) as DateItem)
            is ErrorViewHolder -> holder.onBind(onRetryClicked)
            is HistorySellTransactionViewHolder -> holder.onBind(getItem(position) as MoonpayTransactionItem)
            is ProgressViewHolder -> Unit
        }
    }

    override fun getItemId(position: Int): Long {
        return when (val item = currentList.getOrNull(position)) {
            is TransactionItem -> item.transactionId.hashCode().toLong()
            is DateItem -> item.date.hashCode().toLong()
            else -> RecyclerView.NO_ID
        }
    }

    override fun setHasStableIds(hasStableIds: Boolean) {
        super.setHasStableIds(true)
    }

    override fun getItemViewType(position: Int): Int {
        val preLastItemPosition = itemCount - 1

        val isTransactionItemViewType =
            !pagingController.isPagingRequiresLoadingItem() || position < preLastItemPosition
        val isLoadingItemViewType =
            pagingController.isPagingInLoadingState()
        return when {
            isTransactionItemViewType -> getHistoryItemViewType(position)
            isLoadingItemViewType -> PROGRESS_VIEW_TYPE
            else -> ERROR_VIEW_TYPE
        }
    }

    override fun getItemCount(): Int {
        val additionalItemSize = if (pagingController.isPagingRequiresLoadingItem()) 1 else 0
        return currentList.size + additionalItemSize
    }

    private fun getHistoryItemViewType(position: Int): Int {
        return when (val item = getItem(position)) {
            is DateItem -> HISTORY_DATE_VIEW_TYPE
            is TransactionItem -> getTransactionItemViewType(item)
            is MoonpayTransactionItem -> TRANSACTION_MOONPAY_VIEW_TYPE
        }
    }

    private fun getTransactionItemViewType(item: TransactionItem): Int {
        // TODO migrate on one item in future when we will use UIModel
        val hasSourceOrDestinationTokens = item.sourceIconUrl != null || item.destinationIconUrl != null
        return if (hasSourceOrDestinationTokens) {
            TRANSACTION_SWAP_VIEW_TYPE
        } else {
            TRANSACTION_VIEW_TYPE
        }
    }

    object HistoryItemComparataor : DiffUtil.ItemCallback<HistoryItem>() {
        override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return when {
                oldItem is TransactionItem && newItem is TransactionItem ->
                    oldItem.transactionId == newItem.transactionId
                oldItem is DateItem && newItem is DateItem ->
                    oldItem.date.isSameAs(newItem.date)
                else ->
                    false
            }
        }

        override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return oldItem.transactionId == newItem.transactionId
        }
    }

    fun setPagingState(newState: PagingState) {
        pagingController.setPagingState(newState)
    }

    fun isEmpty() = currentList.isEmpty()

    override fun getRoundedItem(adapterPosition: Int): RoundedItem? {
        val position = if (adapterPosition == currentList.size && needToShowAdditionalItem()) {
            adapterPosition - 1
        } else {
            adapterPosition
        }
        return currentList.getOrNull(position) as? RoundedItem
    }

    private fun needToShowAdditionalItem(): Boolean {
        val isFetchPageError = pagingController.isPagingErrorState() && currentList.isNotEmpty()
        return pagingController.isPagingInLoadingState() || isFetchPageError
    }
}
