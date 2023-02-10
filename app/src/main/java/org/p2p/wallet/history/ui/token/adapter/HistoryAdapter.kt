package org.p2p.wallet.history.ui.token.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.annotation.SuppressLint
import android.view.ViewGroup
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.utils.recycler.RoundedItem
import org.p2p.uikit.utils.recycler.RoundedItemAdapterInterface
import org.p2p.wallet.common.date.isSameAs
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryItem.DateItem
import org.p2p.wallet.history.model.HistoryItem.MoonpayTransactionItem
import org.p2p.wallet.history.model.HistoryItem.TransactionItem
import org.p2p.wallet.history.model.HistoryTransaction
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
) : RecyclerView.Adapter<HistoryTransactionViewHolder>(), RoundedItemAdapterInterface {

    private val currentItems = mutableListOf<HistoryItem>()
    private val pagingController = HistoryAdapterPagingController(this)

    @SuppressLint("NotifyDataSetChanged")
    fun setTransactions(newTransactions: List<HistoryItem>) {
        // force notifyDataSetChanged on first load
        // to fix jumping into the middle because of DiffUtil
        if (currentItems.isEmpty()) {
            currentItems += newTransactions
            notifyDataSetChanged()
        } else {
            val oldItems = ArrayList(currentItems)
            currentItems.clear()
            currentItems += newTransactions

            DiffUtil.calculateDiff(getDiffCallback(oldItems, currentItems))
                .dispatchUpdatesTo(this)
        }
    }

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
            is TransactionViewHolder -> holder.onBind(currentItems[position] as TransactionItem)
            is TransactionSwapViewHolder -> holder.onBind(currentItems[position] as TransactionItem)
            is DateViewHolder -> holder.onBind(currentItems[position] as DateItem)
            is ErrorViewHolder -> holder.onBind(onRetryClicked)
            is HistorySellTransactionViewHolder -> holder.onBind(currentItems[position] as MoonpayTransactionItem)
            is ProgressViewHolder -> Unit
        }
    }

    override fun getItemId(position: Int): Long {
        return when (val item = currentItems.getOrNull(position)) {
            is TransactionItem -> item.transaction.signature.hashCode().toLong()
            is DateItem -> item.date.hashCode().toLong()
            else -> RecyclerView.NO_ID
        }
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
        return currentItems.size + additionalItemSize
    }

    private fun getHistoryItemViewType(position: Int): Int {
        return when (val item = currentItems[position]) {
            is DateItem -> HISTORY_DATE_VIEW_TYPE
            is TransactionItem -> getTransactionItemViewType(item)
            is MoonpayTransactionItem -> TRANSACTION_MOONPAY_VIEW_TYPE
        }
    }

    private fun getTransactionItemViewType(item: TransactionItem): Int {
        return when (item.transaction) {
            is HistoryTransaction.Swap -> TRANSACTION_SWAP_VIEW_TYPE
            else -> TRANSACTION_VIEW_TYPE
        }
    }

    private fun getDiffCallback(
        oldList: List<HistoryItem>,
        newList: List<HistoryItem>
    ): DiffUtil.Callback = object : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return when {
                oldItem is TransactionItem && newItem is TransactionItem ->
                    oldItem.transaction.signature == newItem.transaction.signature
                oldItem is DateItem && newItem is DateItem ->
                    oldItem.date.isSameAs(newItem.date)
                else ->
                    oldItem == newItem
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]
            return old == new
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size
    }

    fun setPagingState(newState: PagingState) {
        pagingController.setPagingState(newState)
    }

    fun isEmpty() = currentItems.isEmpty()

    override fun getRoundedItem(adapterPosition: Int): RoundedItem? {
        val position = if (adapterPosition == currentItems.size && needToShowAdditionalItem()) {
            adapterPosition - 1
        } else {
            adapterPosition
        }
        return currentItems.getOrNull(position) as? RoundedItem
    }

    private fun needToShowAdditionalItem(): Boolean {
        val isFetchPageError = pagingController.isPagingErrorState() && currentItems.isNotEmpty()
        return pagingController.isPagingInLoadingState() || isFetchPageError
    }
}
