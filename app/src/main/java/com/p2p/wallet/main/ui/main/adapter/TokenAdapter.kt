package com.p2p.wallet.main.ui.main.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.R
import com.p2p.wallet.main.model.TokenItem
import com.p2p.wallet.token.model.Token

class TokenAdapter(
    private val onItemClicked: (Token) -> Unit,
    private val onHideClicked: (Token) -> Unit,
    private val onEditClicked: (Token) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        sealed class TokenAdapterItem {
            data class Shown(val token: Token) : TokenAdapterItem()
            data class HiddenGroup(val tokens: List<Token>) : TokenAdapterItem()
        }
    }

    private val data = mutableListOf<TokenAdapterItem>()

    private var isZerosHidden: Boolean = true

    fun setItems(new: List<TokenItem>, isZerosHidden: Boolean) {
        this.isZerosHidden = isZerosHidden

        val old = ArrayList(data)
        data.clear()
        data.addAll(mapGroups(new))
        DiffUtil.calculateDiff(getDiffCallback(old, data)).dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is TokenAdapterItem.Shown -> R.layout.item_token
        is TokenAdapterItem.HiddenGroup -> R.layout.item_token_group
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.item_token ->
            TokenViewHolder(
                parent,
                isZerosHidden,
                onItemClicked,
                onEditClicked,
                onHideClicked
            )
        R.layout.item_token_group ->
            TokenGroupViewHolder(
                parent,
                isZerosHidden,
                onItemClicked,
                onEditClicked,
                onHideClicked
            )
        else -> throw IllegalStateException("Unknown viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TokenViewHolder -> holder.onBind(data[position] as TokenAdapterItem.Shown)
            is TokenGroupViewHolder -> holder.onBind(data[position] as TokenAdapterItem.HiddenGroup)
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

    private fun mapGroups(tokens: List<TokenItem>): List<TokenAdapterItem> {
        val hidden = tokens.filterIsInstance<TokenItem.Hidden>().map { it.token }
        val shown = tokens.filterIsInstance<TokenItem.Shown>().map { TokenAdapterItem.Shown(it.token) }
        return if (hidden.isEmpty()) shown else shown + listOf(TokenAdapterItem.HiddenGroup(hidden))
    }

    private fun getDiffCallback(
        oldList: List<TokenAdapterItem>,
        newList: List<TokenAdapterItem>
    ) = object : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]
            return when {
                old is TokenAdapterItem.Shown && new is TokenAdapterItem.Shown ->
                    old.token.publicKey == new.token.publicKey &&
                        old.token.visibility == new.token.visibility &&
                        old.token.total == new.token.total &&
                        old.token.price == new.token.price
                old is TokenAdapterItem.HiddenGroup && new is TokenAdapterItem.HiddenGroup ->
                    compareInnerList(old.tokens, new.tokens)
                else -> false
            }
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]
            return when {
                old is TokenAdapterItem.Shown && new is TokenAdapterItem.Shown ->
                    old.token == new.token
                old is TokenAdapterItem.HiddenGroup && new is TokenAdapterItem.HiddenGroup ->
                    compareInnerList(old.tokens, new.tokens)
                else -> false
            }
        }

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        private fun compareInnerList(old: List<Token>, new: List<Token>): Boolean {
            if (old.size != new.size) return false

            return old.zip(new).all { (old, new) ->
                old.publicKey == new.publicKey &&
                    old.visibility == new.visibility &&
                    old.total == new.total &&
                    old.price == new.price
            }
        }
    }
}