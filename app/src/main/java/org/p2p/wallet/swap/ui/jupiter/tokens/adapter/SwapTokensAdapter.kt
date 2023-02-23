package org.p2p.wallet.swap.ui.jupiter.tokens.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.uikit.components.finance_block.FinanceBlockCellModel
import org.p2p.uikit.components.finance_block.FinanceBlockViewHolder
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.uikit.organisms.sectionheader.SectionHeaderViewHolder
import org.p2p.wallet.swap.jupiter.domain.model.SwapTokenModel
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class SwapTokensAdapter(
    private val onTokenClicked: (SwapTokenModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<AnyCellItem>()

    fun setTokenItems(tokens: List<AnyCellItem>) {
        items.clear()
        items.addAll(tokens)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (val item = items[position]) {
        is SectionHeaderCellModel -> SectionHeaderViewHolder.DEFAULT_VIEW_TYPE
        is FinanceBlockCellModel -> FinanceBlockViewHolder.DEFAULT_VIEW_TYPE
        else -> error("Not supported cell model: ${item.javaClass.simpleName}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        SectionHeaderViewHolder.DEFAULT_VIEW_TYPE -> {
            SectionHeaderViewHolder(parent.inflateViewBinding(attachToRoot = false))
        }
        FinanceBlockViewHolder.DEFAULT_VIEW_TYPE -> {
            FinanceBlockViewHolder(
                binding = parent.inflateViewBinding(attachToRoot = false),
                inflateListener = {
                    it.setOnClickAction { _, item -> onTokenClicked.invoke(item.payload as SwapTokenModel) }
                }
            )
        }
        else -> error("Not supported viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is SectionHeaderViewHolder -> holder.bind(item as SectionHeaderCellModel)
            is FinanceBlockViewHolder -> holder.bind(item as FinanceBlockCellModel)
        }
    }

    override fun getItemCount(): Int = items.size
}
