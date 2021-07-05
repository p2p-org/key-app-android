package com.p2p.wallet.main.ui.main.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.R
import com.p2p.wallet.main.model.TokenItem
import com.p2p.wallet.main.model.VisibilityState
import com.p2p.wallet.token.model.Token

class TokenAdapter(
    private val onItemClicked: (Token) -> Unit,
    private val onHideClicked: (Token) -> Unit,
    private val onEditClicked: (Token) -> Unit,
    private val onToggleClicked: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<TokenItem>()

    private var isZerosHidden: Boolean = true

    fun setItems(new: List<TokenItem>, isZerosHidden: Boolean, state: VisibilityState) {
        this.isZerosHidden = isZerosHidden

//        new.forEach {
//            when (it) {
//                is TokenItem.Shown -> Timber.d("### shown ${it.token.tokenSymbol} vb: ${it.token.visibility}")
//                is TokenItem.Hidden -> Timber.d("### hidden ${it.token.tokenSymbol} vb: ${it.token.visibility}")
//                is TokenItem.Action -> Timber.d("### action ${it.state}")
//            }
//        }

        val old = ArrayList(data)
        data.clear()
        data.addAll(mapGroups(new, state))
        DiffUtil.calculateDiff(getDiffCallback(old, data)).dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is TokenItem.Shown -> R.layout.item_token
        is TokenItem.Hidden -> R.layout.item_token_hidden
        is TokenItem.Action -> R.layout.item_token_hidden_group_button
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.item_token ->
            TokenViewHolder(
                parent,
                onItemClicked,
                onEditClicked,
                onHideClicked
            )
        R.layout.item_token_hidden ->
            TokenHiddenViewHolder(
                parent,
                onItemClicked,
                onEditClicked,
                onHideClicked
            )
        R.layout.item_token_hidden_group_button ->
            TokenButtonViewHolder(
                parent,
                onToggleClicked
            )
        else -> throw IllegalStateException("Unknown viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TokenViewHolder -> holder.onBind(data[position] as TokenItem.Shown, isZerosHidden)
            is TokenHiddenViewHolder -> holder.onBind(data[position] as TokenItem.Hidden, isZerosHidden)
            is TokenButtonViewHolder -> holder.onBind(data[position] as TokenItem.Action)
        }
    }

    private fun mapGroups(tokens: List<TokenItem>, state: VisibilityState): List<TokenItem> =
        tokens
            .groupBy { it is TokenItem.Hidden }
            .map { (isHidden, list) -> mapHiddenGroup(isHidden, list, state) }
            .flatten()

    private fun mapHiddenGroup(
        isHidden: Boolean,
        list: List<TokenItem>,
        state: VisibilityState
    ) =
        if (isHidden) {
            list.toMutableList().apply {
                add(0, TokenItem.Action(state))
            }
        } else {
            list
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
                        old.token.visibility == new.token.visibility &&
                        old.token.total == new.token.total &&
                        old.token.price == new.token.price
                old is TokenItem.Hidden && new is TokenItem.Hidden ->
                    old.token.publicKey == new.token.publicKey &&
                        old.token.visibility == new.token.visibility &&
                        old.token.total == new.token.total &&
                        old.token.price == new.token.price
                old is TokenItem.Action && new is TokenItem.Action ->
                    old.state == new.state
                else -> false
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