package com.p2p.wallet.main.ui.main.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.R
import com.p2p.wallet.main.model.TokenItem
import com.p2p.wallet.token.model.Token

class TokenAdapter(
    private val onItemClicked: (Token) -> Unit,
    private val onDeleteClicked: (Token) -> Unit,
    private val onEditClicked: (Token) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<TokenItem>()

    fun setItems(new: List<TokenItem>) {
        val old = ArrayList(data)
        data.clear()
        data.addAll(new)

        DiffUtil.calculateDiff(getDiffCallback(old, data)).dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is TokenItem.Shown -> R.layout.item_token
        is TokenItem.Group -> R.layout.item_token_group
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.item_token -> TokenViewHolder(parent, onItemClicked, onEditClicked, onDeleteClicked)
        R.layout.item_token_group -> TokenGroupViewHolder(parent, onItemClicked, onEditClicked, onDeleteClicked)
        else -> throw IllegalStateException("Unknown viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TokenViewHolder -> holder.onBind(data[position] as TokenItem.Shown)
            is TokenGroupViewHolder -> holder.onBind(data[position] as TokenItem.Group)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is TokenGroupViewHolder -> holder.onViewRecycled()
            else -> { /* do nothing */
            }
        }
    }

    private fun getDiffCallback(
        oldList: List<TokenItem>,
        newList: List<TokenItem>
    ) = object : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]
            return when {
                old is TokenItem.Shown && new is TokenItem.Shown ->
                    old.token.publicKey == new.token.publicKey &&
                        old.token.isHidden == new.token.isHidden &&
                        old.token.total == new.token.total &&
                        old.token.price == new.token.price
                old is TokenItem.Group && new is TokenItem.Group ->
                    old.hiddenTokens.size == new.hiddenTokens.size
                else -> false
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]
            return when {
                old is TokenItem.Shown && new is TokenItem.Shown ->
                    old.token == new.token
                old is TokenItem.Group && new is TokenItem.Group ->
                    old.hiddenTokens.size == new.hiddenTokens.size
                else -> false
            }
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size
    }
}