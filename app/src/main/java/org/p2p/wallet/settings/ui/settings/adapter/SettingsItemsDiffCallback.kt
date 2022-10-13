package org.p2p.wallet.settings.ui.settings.adapter

import androidx.recyclerview.widget.DiffUtil
import org.p2p.wallet.settings.model.SettingsItem

class SettingsItemsDiffCallback(
    private val oldList: List<SettingsItem>,
    private val newList: List<SettingsItem>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]

        return old == new
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val old = oldList[oldItemPosition]
        val new = newList[newItemPosition]
        return when {
            old is SettingsItem.SwitchSettingsItem && new is SettingsItem.SwitchSettingsItem ->
                old.nameRes == new.nameRes && old.isSwitched == new.isSwitched
            else -> true
        }
    }

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size
}
