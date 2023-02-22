package org.p2p.uikit.organisms.sectionheader

import androidx.recyclerview.widget.RecyclerView
import org.p2p.uikit.R
import org.p2p.uikit.databinding.ItemSectionHeaderBinding

class SectionHeaderViewHolder(
    private val binding: ItemSectionHeaderBinding
): RecyclerView.ViewHolder(binding.root) {
    companion object {
        val DEFAULT_VIEW_TYPE: Int = R.layout.item_section_header
    }

    fun bind(model: SectionHeaderCellModel) {
        binding.root.bind(model)
    }
}
