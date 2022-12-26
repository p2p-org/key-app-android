package org.p2p.wallet.history.ui.token.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.annotation.SuppressLint
import android.view.ViewGroup
import org.p2p.core.glide.GlideManager
import org.p2p.wallet.common.date.isSameAs
import org.p2p.wallet.common.date.isSameDayAs
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryItem.DateItem
import org.p2p.wallet.history.model.HistoryItem.Empty
import org.p2p.wallet.history.model.HistoryItem.MoonpayTransactionItem
import org.p2p.wallet.history.model.HistoryItem.TransactionItem
import org.p2p.wallet.history.model.HistoryTransaction
import org.p2p.wallet.history.ui.token.adapter.holders.DateViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.EmptyViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.ErrorViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.HistoryTransactionViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.MoonpayTransactionViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.ProgressViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.TransactionSwapViewHolder
import org.p2p.wallet.history.ui.token.adapter.holders.TransactionViewHolder
import org.p2p.wallet.sell.ui.lock.SellTransactionViewDetails

private const val TRANSACTION_VIEW_TYPE = 1
private const val HISTORY_EMPTY_VIEW_TYPE = 2
private const val HISTORY_DATE_VIEW_TYPE = 3
private const val PROGRESS_VIEW_TYPE = 4
private const val ERROR_VIEW_TYPE = 5
private const val TRANSACTION_SWAP_VIEW_TYPE = 6
private const val TRANSACTION_MOONPAY_VIEW_TYPE = 7

class HistoryAdapter(
    private val glideManager: GlideManager,
    private val onTransactionClicked: (HistoryTransaction) -> Unit,
    private val onMoonpayTransactionClicked: (SellTransactionViewDetails) -> Unit,
    private val onRetryClicked: () -> Unit
) : RecyclerView.Adapter<HistoryTransactionViewHolder>() {

    private val currentItems = mutableListOf<HistoryItem>()
    private val pagingController = HistoryAdapterPagingController(this)

    @SuppressLint("NotifyDataSetChanged")
    fun setTransactions(
        newTransactions: List<HistoryTransaction>,
        newMoonpayTransactions: List<MoonpayTransactionItem>
    ) {
        // force notifyDataSetChanged on first load
        // to fix jumping into the middle because of DiffUtil
        if (currentItems.isEmpty()) {
            currentItems += newMoonpayTransactions // goes first
            currentItems += newTransactions.mapToItems()
            notifyDataSetChanged()
        } else {
            val oldItems = ArrayList(currentItems)
            currentItems.clear()
            currentItems += newMoonpayTransactions // goes first
            currentItems += newTransactions.mapToItems()

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
            TRANSACTION_MOONPAY_VIEW_TYPE -> MoonpayTransactionViewHolder(parent, onMoonpayTransactionClicked)
            else -> ErrorViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: HistoryTransactionViewHolder, position: Int) {
        when (holder) {
            is TransactionViewHolder -> holder.onBind(currentItems[position] as TransactionItem)
            is TransactionSwapViewHolder -> holder.onBind(currentItems[position] as TransactionItem)
            is DateViewHolder -> holder.onBind(currentItems[position] as DateItem)
            is ErrorViewHolder -> holder.onBind(pagingController.currentPagingState, onRetryClicked)
            is MoonpayTransactionViewHolder -> holder.onBind(currentItems[position] as MoonpayTransactionItem)
            is EmptyViewHolder -> Unit
            is ProgressViewHolder -> Unit
        }
    }

    override fun getItemId(position: Int): Long {
        return when (val item = currentItems.getOrNull(position)) {
            is TransactionItem -> item.transaction.signature.hashCode().toLong()
            is DateItem -> item.date.hashCode().toLong()
            is Empty -> position.hashCode().toLong()
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
            is Empty -> HISTORY_EMPTY_VIEW_TYPE
            is MoonpayTransactionItem -> TRANSACTION_MOONPAY_VIEW_TYPE
        }
    }

    private fun getTransactionItemViewType(item: TransactionItem): Int {
        return when (item.transaction) {
            is HistoryTransaction.Swap -> TRANSACTION_SWAP_VIEW_TYPE
            else -> TRANSACTION_VIEW_TYPE
        }
    }

    private fun List<HistoryTransaction>.mapToItems(): List<HistoryItem> = flatMapIndexed { i, transaction ->
        val isCurrentAndPreviousTransactionOnSameDay = i > 0 && get(i - 1).date.isSameDayAs(transaction.date)
        if (isCurrentAndPreviousTransactionOnSameDay) {
            listOf(TransactionItem(transaction))
        } else {
            listOf(
                DateItem(transaction.date),
                // todo map items according to state
                TransactionItem(transaction)
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
}
