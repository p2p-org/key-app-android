package org.p2p.wallet.jupiter.ui.tokens.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import org.p2p.uikit.components.finance_block.MainCellModel
import org.p2p.uikit.components.finance_block.MainCellViewHolder
import org.p2p.uikit.model.AnyCellItem
import org.p2p.uikit.organisms.sectionheader.SectionHeaderCellModel
import org.p2p.uikit.organisms.sectionheader.SectionHeaderViewHolder
import org.p2p.uikit.utils.recycler.CustomBaseAdapter
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.jupiter.ui.tokens.presenter.SwapTokensCellModelPayload
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class SwapTokensAdapter(
    private val onTokenClicked: (SwapTokenModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), CustomBaseAdapter {

    private val items = mutableListOf<AnyCellItem>()

    fun setTokenItems(tokens: List<AnyCellItem>) {
        items.clear()
        items.addAll(tokens)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (val item = items[position]) {
        is SectionHeaderCellModel -> SectionHeaderViewHolder.DEFAULT_VIEW_TYPE
        is MainCellModel -> MainCellViewHolder.DEFAULT_VIEW_TYPE
        else -> error("Not supported cell model: ${item.javaClass.simpleName}")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        SectionHeaderViewHolder.DEFAULT_VIEW_TYPE -> {
            SectionHeaderViewHolder(parent.inflateViewBinding(attachToRoot = false))
        }
        MainCellViewHolder.DEFAULT_VIEW_TYPE -> {
            MainCellViewHolder(
                binding = parent.inflateViewBinding(attachToRoot = false),
                inflateListener = {
                    it.setOnClickAction { _, item ->
                        onTokenClicked.invoke(item.typedPayload<SwapTokensCellModelPayload>().tokenModel)
                    }
                }
            )
        }
        else -> error("Not supported viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is SectionHeaderViewHolder -> holder.bind(item as SectionHeaderCellModel)
            is MainCellViewHolder -> holder.bind(item as MainCellModel)
        }
    }

    override fun getItemCount(): Int = items.size
    override fun getItems(): List<AnyCellItem> = items
}
