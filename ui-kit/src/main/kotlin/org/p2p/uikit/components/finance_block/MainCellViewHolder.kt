package org.p2p.uikit.components.finance_block

import androidx.recyclerview.widget.RecyclerView
import org.p2p.uikit.R
import org.p2p.uikit.databinding.ItemMainCellBinding

class MainCellViewHolder(
    private val binding: ItemMainCellBinding,
    inflateListener: ((financeBlock: UiKitMainCellView) -> Unit)? = null,
    private val onBindListener: ((view: UiKitMainCellView, item: MainCellModel) -> Unit)? = null,
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        val DEFAULT_VIEW_TYPE: Int = R.layout.item_main_cell.plus(MainCellStyle.FINANCE_BLOCK.ordinal)
    }

    init {
        binding.root.bindViewStyle(MainCellStyle.FINANCE_BLOCK)
        inflateListener?.invoke(binding.root)
    }

    val item: MainCellModel
        get() = binding.root.item

    fun bind(model: MainCellModel) {
        binding.root.bind(model)
        onBindListener?.invoke(binding.root, model)
    }

    inline fun <reified T : Any> getPayload(): T = this.item.payload as T
    inline fun <reified T : Any> getPayloadOrNull(): T? = this.item.payload as? T
}

val RecyclerView.ViewHolder?.asFinanceCell: MainCellViewHolder?
    get() = this as? MainCellViewHolder
