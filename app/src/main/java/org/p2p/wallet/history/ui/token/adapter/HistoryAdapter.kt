package org.p2p.wallet.history.ui.token.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.common.date.isSameAs
import org.p2p.wallet.common.date.isSameDayAs
import org.p2p.wallet.common.glide.GlideManager
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.ui.token.adapter.holders.DateViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.EmptyViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.ErrorViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.HistoryTransactionViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.ProgressViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.TransactionSwapViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.TransactionViewHolder
import org.p2p.wallet.utils.NoOp

private const val TRANSACTION_VIEW_TYPE = 1
private const val HISTORY_EMPTY_VIEW_TYPE = 2
private const val HISTORY_DATE_VIEW_TYPE = 3
private const val PROGRESS_VIEW_TYPE = 4
private const val ERROR_VIEW_TYPE = 5
private const val TRANSACTION_SWAP_VIEW_TYPE = 6

class HistoryAdapter(
    private val glideManager: GlideManager,
    private val onTransactionClicked: (HistoryTransaction) -> Unit,
    private val onRetryClicked: () -> Unit
) : RecyclerView.Adapter<HistoryTransactionViewHolder>() {

    private var currentItems = mutableListOf<HistoryItem>()
    private val pagingController = HistoryAdapterPagingController(this)

    @SuppressLint("NotifyDataSetChanged")
    fun setTransactions(newTransactions: List<HistoryTransaction>) {
        // force notifyDataSetChanged on first load
        // to fix jumping into the middle because of DiffUtil
        if (currentItems.isEmpty()) {
            currentItems += newTransactions.mapToItems()
            notifyDataSetChanged()
        } else {
            val oldItems = ArrayList(currentItems)
            currentItems = newTransactions.mapToItems().toMutableList()

            DiffUtil.calculateDiff(getDiffCallback(oldItems, currentItems))
                .dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryTransactionViewHolder {
        return when (viewType) {
            TRANSACTION_VIEW_TYPE -> TransactionViewHolder(parent, onTransactionClicked)
            TRANSACTION_SWAP_VIEW_TYPE -> TransactionSwapViewHolder(parent, glideManager, onTransactionClicked)
            HISTORY_EMPTY_VIEW_TYPE -> EmptyViewHolder(parent)
            HISTORY_DATE_VIEW_TYPE -> DateViewHolder(parent)
            PROGRESS_VIEW_TYPE -> ProgressViewHolder(parent)
            else -> ErrorViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: HistoryTransactionViewHolder, position: Int) {
        when (holder) {
            is TransactionViewHolder -> holder.onBind(currentItems[position] as HistoryItem.TransactionItem)
            is TransactionSwapViewHolder -> holder.onBind(currentItems[position] as HistoryItem.TransactionItem)
            is DateViewHolder -> holder.onBind(currentItems[position] as HistoryItem.DateItem)
            is ErrorViewHolder -> holder.onBind(pagingController.currentPagingState, onRetryClicked)
            else -> NoOp
        }
    }

    override fun getItemId(position: Int): Long {
        return when (val item = currentItems.getOrNull(position)) {
            is HistoryItem.TransactionItem -> item.transaction.signature.hashCode().toLong()
            is HistoryItem.DateItem -> item.date.hashCode().toLong()
            is HistoryItem.Empty -> position.hashCode().toLong()
            else -> RecyclerView.NO_ID
        }
    }

    override fun getItemViewType(position: Int): Int {
        val preLastItemPosition = itemCount - 1

        // todo: refactor to AdapterDelegate
        //  and create item list with progress item inside instead of implicitly adding it here
        //  DISCUSS ON ANDROID MEETING
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
            is HistoryItem.DateItem -> HISTORY_DATE_VIEW_TYPE
            is HistoryItem.TransactionItem -> getTransactionItemViewType(item)
            is HistoryItem.Empty -> HISTORY_EMPTY_VIEW_TYPE
        }
    }

    private fun getTransactionItemViewType(item: HistoryItem.TransactionItem): Int {
        return when (item.transaction) {
            is HistoryTransaction.Swap -> TRANSACTION_SWAP_VIEW_TYPE
            else -> TRANSACTION_VIEW_TYPE
        }
    }

    private fun List<HistoryTransaction>.mapToItems(): List<HistoryItem> = flatMapIndexed { i, transaction ->
        val isCurrentAndPreviousTransactionOnSameDay = i > 0 && get(i - 1).date.isSameDayAs(transaction.date)
        if (isCurrentAndPreviousTransactionOnSameDay) {
            listOf(HistoryItem.TransactionItem(transaction))
        } else {
            listOf(
                HistoryItem.DateItem(transaction.date),
                // todo map items according to state
                HistoryItem.TransactionItem(transaction)
            )
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
                oldItem is HistoryItem.TransactionItem && newItem is HistoryItem.TransactionItem ->
                    oldItem.transaction.signature == newItem.transaction.signature
                oldItem is HistoryItem.DateItem && newItem is HistoryItem.DateItem ->
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
}
