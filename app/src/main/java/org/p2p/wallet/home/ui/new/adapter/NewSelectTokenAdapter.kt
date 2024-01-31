package org.p2p.wallet.home.ui.new.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.core.token.Token
import org.p2p.wallet.R
import org.p2p.wallet.home.model.SelectTokenItem
import org.p2p.wallet.home.model.SelectTokenItem.CategoryTitle
import org.p2p.wallet.home.model.SelectTokenItem.SelectableToken

class NewSelectTokenAdapter(
    private val onItemClicked: (Token.Active) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val DIFF_FIELD_ROUND_STATE = "DIFF_FIELD_ROUND_STATE"
    }

    private val data = mutableListOf<SelectTokenItem>()

    fun setItems(new: List<SelectTokenItem>) {
        val old = data.toMutableList()
        data.clear()
        data.addAll(new)
        DiffUtil.calculateDiff(NewTokenAdapterDiffCallback(old, data)).dispatchUpdatesTo(this)
    }

    fun clear() {
        val size = data.size
        data.clear()
        notifyItemRangeRemoved(0, size)
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is SelectableToken -> R.layout.item_pick_token_new
        is CategoryTitle -> R.layout.item_pick_token_category
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        R.layout.item_pick_token_new -> NewSelectTokenViewHolder(parent, onItemClicked)
        R.layout.item_pick_token_category -> CategoryTitleViewHolder(parent)
        else -> error("Unknown type: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CategoryTitleViewHolder -> holder.onBind(data[position] as CategoryTitle)
            is NewSelectTokenViewHolder -> holder.onBind(data[position] as SelectableToken)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        val fields = payloads.firstOrNull() as? Set<String>

        if (fields.isNullOrEmpty()) {
            onBindViewHolder(holder, position)
            return
        }

        val item = data[position]
        fields.forEach { field ->
            when (field) {
                DIFF_FIELD_ROUND_STATE -> (holder as NewSelectTokenViewHolder).setCornersRadius(item as SelectableToken)
            }
        }
    }
}
