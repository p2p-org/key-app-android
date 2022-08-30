package org.p2p.wallet.settings.ui.settings.adapter

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import android.view.ViewGroup
import org.p2p.wallet.R
import org.p2p.wallet.settings.ui.settings.presenter.SettingsItem
import org.p2p.wallet.settings.ui.settings.adapter.viewholder.ComplexSettingViewHolder
import org.p2p.wallet.settings.ui.settings.adapter.viewholder.SettingsGroupTitleViewHolder
import org.p2p.wallet.settings.ui.settings.adapter.viewholder.SettingsSpaceSeparatorViewHolder
import org.p2p.wallet.settings.ui.settings.adapter.viewholder.SettingsViewHolder
import org.p2p.wallet.settings.ui.settings.adapter.viewholder.SignOutButtonViewHolder
import org.p2p.wallet.settings.ui.settings.adapter.viewholder.SwitchSettingViewHolder
import org.p2p.wallet.settings.ui.settings.adapter.viewholder.TextSettingViewHolder
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class SettingsAdapter(
    private val settingsItemClickListener: SettingsItemClickListener
) : RecyclerView.Adapter<SettingsViewHolder<*, *>>() {

    private val items = mutableListOf<SettingsItem>()

    fun setItems(newItems: List<SettingsItem>) {
        items.clear()
        items.addAll(newItems)
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is SettingsItem.SettingsGroupTitleItem -> R.layout.item_settings_group_title
        is SettingsItem.ComplexSettingItem -> R.layout.item_settings_complex
        is SettingsItem.SettingsSpaceSeparatorItem -> R.layout.item_settings_space_separator
        is SettingsItem.SignOutButtonItem -> R.layout.item_settings_sign_out
        is SettingsItem.SwitchSettingItem -> R.layout.item_settings_switch
        is SettingsItem.TextSettingItem -> R.layout.item_settings_text_value
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsViewHolder<*, *> = when (viewType) {
        R.layout.item_settings_group_title ->
            SettingsGroupTitleViewHolder(parent.inflateViewBindingForViewHolder())
        R.layout.item_settings_complex ->
            ComplexSettingViewHolder(parent.inflateViewBindingForViewHolder(), settingsItemClickListener)
        R.layout.item_settings_space_separator ->
            SettingsSpaceSeparatorViewHolder(parent.inflateViewBindingForViewHolder())
        R.layout.item_settings_sign_out ->
            SignOutButtonViewHolder(parent.inflateViewBindingForViewHolder(), settingsItemClickListener)
        R.layout.item_settings_switch ->
            SwitchSettingViewHolder(parent.inflateViewBindingForViewHolder(), settingsItemClickListener)
        R.layout.item_settings_text_value ->
            TextSettingViewHolder(parent.inflateViewBindingForViewHolder())
        else ->
            error("ViewHolder with view type $viewType is not found")
    }

    override fun onBindViewHolder(holder: SettingsViewHolder<*, *>, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    private inline fun <reified VB : ViewBinding> ViewGroup.inflateViewBindingForViewHolder(): VB {
        return inflateViewBinding(attachToRoot = false)
    }
}
