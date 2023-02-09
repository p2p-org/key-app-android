package org.p2p.uikit.components.finance_block

import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import org.p2p.uikit.databinding.UiKitFinanceBlockBinding

class FinanceBlockViewHolder(
    private val binding: UiKitFinanceBlockBinding,
) : RecyclerView.ViewHolder(binding.root) {

    var item: FinanceBlockUiModel? = null
        private set

    fun bind(model: FinanceBlockUiModel) {
        item = model
        binding.leftSideView.isVisible = model.leftSideUiModel != null
        model.leftSideUiModel?.let { binding.leftSideView.bind(it) }
    }
}