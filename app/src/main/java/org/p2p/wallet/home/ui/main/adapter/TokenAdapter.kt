package org.p2p.wallet.home.ui.main.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.R
import org.p2p.wallet.home.model.HomeElementItem
import org.p2p.wallet.home.model.VisibilityState

class TokenAdapter(
    private val listener: OnHomeItemsClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<HomeElementItem>()

    private var isZerosHidden: Boolean = true

    fun setItems(new: List<HomeElementItem>, isZerosHidden: Boolean, state: VisibilityState) {
        this.isZerosHidden = isZerosHidden
        val old = data.toMutableList()
        data.clear()
        data.addAll(mapGroups(new, state))
        DiffUtil.calculateDiff(getDiffCallback(old, data)).dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is HomeElementItem.Shown -> R.layout.item_token
        is HomeElementItem.Hidden -> R.layout.item_token_hidden
        is HomeElementItem.Action -> R.layout.item_token_group_button
        is HomeElementItem.Banners -> R.layout.item_banners
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.item_token -> TokenViewHolder(parent, listener)
        R.layout.item_token_hidden -> TokenHiddenViewHolder(parent, listener)
        R.layout.item_token_group_button -> TokenButtonViewHolder(parent, listener)
        R.layout.item_banners -> BannersViewHolder(parent, listener)
        else -> throw IllegalStateException("Unknown viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TokenViewHolder -> holder.onBind(data[position] as HomeElementItem.Shown, isZerosHidden)
            is TokenHiddenViewHolder -> holder.onBind(data[position] as HomeElementItem.Hidden, isZerosHidden)
            is TokenButtonViewHolder -> holder.onBind(data[position] as HomeElementItem.Action)
            is BannersViewHolder -> holder.onBind(data[position] as HomeElementItem.Banners)
        }
    }

    private fun mapGroups(tokens: List<HomeElementItem>, state: VisibilityState): List<HomeElementItem> =
        tokens
            .groupBy { it is HomeElementItem.Hidden }
            .map { (isHidden, list) -> mapTokensGroup(isHidden, list, state) }
            .flatten()

    private fun mapTokensGroup(
        isHidden: Boolean,
        list: List<HomeElementItem>,
        state: VisibilityState
    ) = list.toMutableList().apply {
        add(0, HomeElementItem.Action(state, isHidden))
    }

    private fun getDiffCallback(
        oldList: List<HomeElementItem>,
        newList: List<HomeElementItem>
    ) = object : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val old = oldList[oldItemPosition]
            val new = newList[newItemPosition]
            return when {
                old is HomeElementItem.Shown && new is HomeElementItem.Shown ->
                    old.token.publicKey == new.token.publicKey &&
                        old.token.mintAddress == new.token.mintAddress &&
                        old.token.visibility == new.token.visibility &&
                        old.token.total == new.token.total &&
                        old.token.totalInUsd == new.token.totalInUsd
                old is HomeElementItem.Hidden && new is HomeElementItem.Hidden ->
                    old.token.publicKey == new.token.publicKey &&
                        old.token.mintAddress == new.token.mintAddress &&
                        old.token.visibility == new.token.visibility &&
                        old.token.total == new.token.total &&
                        old.token.totalInUsd == new.token.totalInUsd
                old is HomeElementItem.Action && new is HomeElementItem.Action ->
                    old.state == new.state
                old is HomeElementItem.Banners && new is HomeElementItem.Banners ->
                    old.banners.size == new.banners.size
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
