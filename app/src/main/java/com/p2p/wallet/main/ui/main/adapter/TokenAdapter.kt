package com.p2p.wallet.main.ui.main.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.R
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.main.model.TokenItem

class TokenAdapter(
    private val onItemClicked: (Token) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<TokenItem>()

    fun setItems(new: List<TokenItem>) {
        data.clear()
        data.addAll(new)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is TokenItem.Shown -> R.layout.item_token
        is TokenItem.Group -> R.layout.item_token_group
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.item_token -> TokenViewHolder(parent, onItemClicked)
        R.layout.item_token_group -> TokenGroupViewHolder(parent)
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
}