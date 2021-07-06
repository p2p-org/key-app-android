package com.p2p.wallet.token.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.R
import com.p2p.wallet.common.date.isSameDayAs
import com.p2p.wallet.common.ui.PagingState
import com.p2p.wallet.token.model.Transaction
import com.p2p.wallet.token.model.TransactionOrDateItem

class HistoryAdapter(
    private val onTransactionClicked: (Transaction) -> Unit,
    private val onRetryClicked: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private val noAdditionalItemRequiredState = listOf(PagingState.Idle)
    }

    private var data: MutableList<TransactionOrDateItem> = mutableListOf()
    private var pagingState: PagingState = PagingState.Idle

    fun setData(new: List<Transaction>) {
        val old = ArrayList(data)

        data.clear()
        data.addAll(mapTransactions(new))

        DiffUtil.calculateDiff(getDiffCallback(old, data)).dispatchUpdatesTo(this)
    }

    /* todo: add paging */
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
            R.layout.item_history_date -> DateViewHolder(parent)
            R.layout.item_progress -> ProgressViewHolder(parent)
            else -> ErrorViewHolder(parent)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TransactionViewHolder -> holder.onBind(data[position] as TransactionOrDateItem.TransactionItem)
            is DateViewHolder -> holder.onBind(data[position] as TransactionOrDateItem.DateItem)
            is ErrorViewHolder -> holder.onBind(R.string.error_general_message, onRetryClicked)
            is ProgressViewHolder -> {
                // do nothing
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return when (val item = if (position < data.size) data[position] else null) {
            is TransactionOrDateItem.TransactionItem -> item.transaction.signature.hashCode().toLong()
            is TransactionOrDateItem.DateItem -> item.date.hashCode().toLong()
            else -> RecyclerView.NO_ID
        }
    }

    override fun getItemViewType(position: Int): Int = when {
        !stateRequiresExtraItem(pagingState) || position < itemCount - 1 -> getAdapterViewType(position)
        pagingState is PagingState.Loading -> R.layout.item_progress
        else -> R.layout.item_error
    }

    override fun getItemCount(): Int =
        data.size + if (stateRequiresExtraItem(pagingState)) 1 else 0

    private fun stateRequiresExtraItem(state: PagingState) = state !in noAdditionalItemRequiredState

    private fun getAdapterViewType(position: Int): Int = when (data[position]) {
        is TransactionOrDateItem.DateItem -> R.layout.item_history_date
        is TransactionOrDateItem.TransactionItem -> R.layout.item_transaction
    }

    private fun mapTransactions(new: List<Transaction>): List<TransactionOrDateItem> =
        new.withIndex().flatMap { (i, transaction) ->
            when {
                i > 0 && new[i - 1].date.isSameDayAs(transaction.date) ->
                    listOf(TransactionOrDateItem.TransactionItem(transaction))
                else ->
                    listOf(
                        TransactionOrDateItem.DateItem(transaction.date),
                        TransactionOrDateItem.TransactionItem(transaction)
                    )
            }
        }

    private fun getDiffCallback(oldList: List<TransactionOrDateItem>, newList: List<TransactionOrDateItem>) =
        object : DiffUtil.Callback() {

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = oldList[oldItemPosition]
                val new = newList[newItemPosition]
                return when {
                    old is TransactionOrDateItem.DateItem && new is TransactionOrDateItem.DateItem ->
                        old.date.isSameDayAs(new.date)
                    old is TransactionOrDateItem.TransactionItem && new is TransactionOrDateItem.TransactionItem ->
                        old.transaction.signature == new.transaction.signature
                    else ->
                        false
                }
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = oldList[oldItemPosition]
                val new = newList[newItemPosition]
                return when {
                    old is TransactionOrDateItem.DateItem && new is TransactionOrDateItem.DateItem ->
                        old.date == new.date
                    old is TransactionOrDateItem.TransactionItem && new is TransactionOrDateItem.TransactionItem ->
                        old.transaction == new.transaction
                    else ->
                        false
                }
            }

            override fun getOldListSize(): Int = oldList.size

            override fun getNewListSize(): Int = newList.size
        }
}