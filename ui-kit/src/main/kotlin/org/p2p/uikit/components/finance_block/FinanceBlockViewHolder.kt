package org.p2p.uikit.components.finance_block

import androidx.recyclerview.widget.RecyclerView
import org.p2p.uikit.R
import org.p2p.uikit.databinding.ItemFinanceBlockBinding

class FinanceBlockViewHolder(
    private val binding: ItemFinanceBlockBinding,
    private val inflateListener: ((financeBlock: UiKitFinanceBlockView) -> Unit)? = null,
    private val onBindListener: ((view: UiKitFinanceBlockView, item: FinanceBlockCellModel) -> Unit)? = null,
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        val DEFAULT_VIEW_TYPE: Int = R.layout.item_finance_block
    }

    init {
        inflateListener?.invoke(binding.root)
    }

    val item: FinanceBlockCellModel
        get() = binding.root.item

    fun bind(model: FinanceBlockCellModel) {
        binding.root.bind(item)
        onBindListener?.invoke(binding.root, item)
    }
}
