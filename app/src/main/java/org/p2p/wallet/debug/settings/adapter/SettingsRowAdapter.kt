package org.p2p.wallet.debug.settings.adapter

import androidx.recyclerview.widget.DiffUtil
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import java.util.Objects
import org.p2p.wallet.settings.model.SettingsRow

class SettingsRowAdapter(
    vararg delegates: AdapterDelegate<List<SettingsRow>>,
) : AsyncListDifferDelegationAdapter<SettingsRow>(DefaultDiffCallback(), *delegates)

private class DefaultDiffCallback : DiffUtil.ItemCallback<SettingsRow>() {

    override fun areItemsTheSame(oldItem: SettingsRow, newItem: SettingsRow): Boolean {
        return oldItem::class == newItem::class
    }

    override fun areContentsTheSame(oldItem: SettingsRow, newItem: SettingsRow): Boolean {
        return Objects.equals(oldItem, newItem)
    }
}
