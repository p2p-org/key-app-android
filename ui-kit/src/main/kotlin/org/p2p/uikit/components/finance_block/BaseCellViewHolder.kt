package org.p2p.uikit.components.finance_block

import androidx.recyclerview.widget.RecyclerView
import org.p2p.uikit.R
import org.p2p.uikit.databinding.ItemMainCellBinding

class BaseCellViewHolder(
    private val binding: ItemMainCellBinding,
    inflateListener: ((financeBlock: UiKitMainCellView) -> Unit)? = null,
    private val onBindListener: ((view: UiKitMainCellView, item: MainCellModel) -> Unit)? = null,
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        val DEFAULT_VIEW_TYPE: Int = R.layout.item_main_cell.plus(MainCellStyle.BASE_CELL.ordinal)
    }

    init {
        binding.root.bindViewStyle(MainCellStyle.BASE_CELL)
        inflateListener?.invoke(binding.root)
    }

    val item: MainCellModel
        get() = binding.root.item

    fun bind(model: MainCellModel) {
        binding.root.bind(model)
        onBindListener?.invoke(binding.root, model)
    }
}
