package org.p2p.uikit.organisms.sectionheader

import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.uikit.R
import org.p2p.uikit.databinding.ItemSectionHeaderBinding
import org.p2p.uikit.model.AnyCellItem

class SectionHeaderViewHolder(
    private val binding: ItemSectionHeaderBinding
) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        val DEFAULT_VIEW_TYPE: Int = R.layout.item_section_header
    }

    fun bind(model: SectionHeaderCellModel) {
        binding.root.bind(model)
    }
}

fun sectionHeaderCellDelegate(): AdapterDelegate<List<AnyCellItem>> =
    adapterDelegateViewBinding<SectionHeaderCellModel, AnyCellItem, ItemSectionHeaderBinding>(
        viewBinding = { inflater, parent -> ItemSectionHeaderBinding.inflate(inflater, parent, false) },
    ) {

        bind {
            binding.root.bind(item)
        }
    }
