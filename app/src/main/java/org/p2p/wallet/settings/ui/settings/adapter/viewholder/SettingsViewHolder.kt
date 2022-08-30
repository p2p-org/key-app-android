package org.p2p.wallet.settings.ui.settings.adapter.viewholder

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem

sealed class SettingsViewHolder<VB : ViewBinding, ITEM : SettingsItem>(
    private val binding: VB
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: SettingsItem) {
        binding.bind(item as ITEM)
    }

    protected abstract fun VB.bind(item: ITEM)
}
