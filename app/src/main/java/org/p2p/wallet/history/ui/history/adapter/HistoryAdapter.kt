package org.p2p.wallet.history.ui.history.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.R
import org.p2p.wallet.common.date.isSameAs
import org.p2p.wallet.common.date.isSameDayAs
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.history.model.HistoryItem
import org.p2p.wallet.history.model.HistoryTransaction

class HistoryAdapter(
    private val onTransactionClicked: (HistoryTransaction) -> Unit,
    private val onRetryClicked: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private val noAdditionalItemRequiredState = listOf(PagingState.Idle)
    }

    private var data: MutableList<HistoryItem> = mutableListOf()
    private var pagingState: PagingState = PagingState.Idle

    fun setTransactions(new: List<HistoryTransaction>) {
        val old = ArrayList(data)
        data.clear()
        data.addAll(mapTransactions(new))
        DiffUtil.calculateDiff(getDiffCallback(old, data)).dispatchUpdatesTo(this)
    }

    fun setPagingState(newState: PagingState) {
        if (pagingState::class.java == newState::class.java) return
        val shouldHasExtraItem = stateRequiresExtraItem(newState)
        val hasExtraItem = stateRequiresExtraItem(pagingState)

        pagingState = newState

        // since item count is a function - cache its value.
        val count = itemCount
        when {
            hasExtraItem && shouldHasExtraItem -> notifyItemChanged(count - 1)
            hasExtraItem && !shouldHasExtraItem -> notifyItemRemoved(count - 1)
            !hasExtraItem && shouldHasExtraItem -> notifyItemInserted(count)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            R.layout.item_transaction -> TransactionViewHolder(parent, onTransactionClicked)
            R.layout.item_history_empty -> EmptyViewHolder(parent)
            R.layout.item_history_date -> DateViewHolder(parent)
            R.layout.item_progress -> ProgressViewHolder(parent)
            else -> ErrorViewHolder(parent)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TransactionViewHolder -> holder.onBind(data[position] as HistoryItem.TransactionItem)
            is DateViewHolder -> holder.onBind(data[position] as HistoryItem.DateItem)
            is ErrorViewHolder -> {
                val item = pagingState as? PagingState.Error
                if (item != null) {
                    holder.onBind(item.e.message.orEmpty(), onRetryClicked)
                } else {
                    holder.onBind(R.string.error_general_message, onRetryClicked)
                }
            }
            is ProgressViewHolder -> {
                // do nothing
            }
            is EmptyViewHolder -> {
                // do nothing
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return when (val item = if (position < data.size) data[position] else null) {
            is HistoryItem.TransactionItem -> item.transaction.signature.hashCode().toLong()
            is HistoryItem.DateItem -> item.date.hashCode().toLong()
            is HistoryItem.Empty -> position.hashCode().toLong()
            else -> RecyclerView.NO_ID
        }
    }

    override fun getItemViewType(position: Int): Int = when {
        !stateRequiresExtraItem(pagingState) || position < itemCount - 1 -> getAdapterViewType(position)
        pagingState is PagingState.Loading || pagingState is PagingState.InitialLoading -> R.layout.item_progress
        else -> R.layout.item_error
    }

    override fun getItemCount(): Int =
        data.size + if (stateRequiresExtraItem(pagingState)) 1 else 0

    private fun stateRequiresExtraItem(state: PagingState) = state !in noAdditionalItemRequiredState

    private fun getAdapterViewType(position: Int): Int = when (data[position]) {
        is HistoryItem.DateItem -> R.layout.item_history_date
        is HistoryItem.TransactionItem -> R.layout.item_transaction
        is HistoryItem.Empty -> R.layout.item_history_empty
    }

    private fun mapTransactions(new: List<HistoryTransaction>): List<HistoryItem> =
        new.withIndex().flatMap { (i, transaction) ->
            when {
                i > 0 && new[i - 1].date.isSameDayAs(transaction.date) ->
                    listOf(HistoryItem.TransactionItem(transaction))
                else ->
                    listOf(
                        HistoryItem.DateItem(transaction.date),
                        HistoryItem.TransactionItem(transaction)
                    )
            }
        }

    private fun getDiffCallback(
        oldList: List<HistoryItem>,
        newList: List<HistoryItem>
    ) = object : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return when {
                oldItem is HistoryItem.TransactionItem && newItem is HistoryItem.TransactionItem ->
                    oldItem.transaction.signature == newItem.transaction.signature
                oldItem is HistoryItem.DateItem && newItem is HistoryItem.DateItem ->
                    oldItem.date.isSameAs(newItem.date)
                else -> oldItem == newItem
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
}
