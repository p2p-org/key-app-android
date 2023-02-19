package org.p2p.wallet.swap.ui.jupiter.tokens.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.uikit.components.finance_block.FinanceBlockViewHolder
import org.p2p.uikit.organisms.sectionheader.SectionHeaderViewHolder
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class SwapTokensAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<SwapTokenItem>()

    fun setTokenItems(tokens: List<SwapTokenItem>) {
        items.clear()
        items.addAll(tokens)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is SwapTokenItem.TokenSectionHeader -> SectionHeaderViewHolder.DEFAULT_VIEW_TYPE
        is SwapTokenItem.SwapTokenFinanceBlock -> FinanceBlockViewHolder.DEFAULT_VIEW_TYPE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        SectionHeaderViewHolder.DEFAULT_VIEW_TYPE -> {
            SectionHeaderViewHolder(parent.inflateViewBinding(attachToRoot = false))
        }
        FinanceBlockViewHolder.DEFAULT_VIEW_TYPE -> {
            FinanceBlockViewHolder(parent.inflateViewBinding(attachToRoot = false))
        }
        else -> error("Not supported viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is SectionHeaderViewHolder -> {
                item as SwapTokenItem.TokenSectionHeader
                holder.bind(item.model)
            }
            is FinanceBlockViewHolder -> {
                item as SwapTokenItem.SwapTokenFinanceBlock
                holder.bind(item.model)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
