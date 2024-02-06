package org.p2p.wallet.settings.ui.settings.adapter

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import android.view.ViewGroup
import org.p2p.wallet.R
import org.p2p.wallet.settings.model.SettingsItem
import org.p2p.wallet.settings.model.SettingsItem.ComplexSettingsItem
import org.p2p.wallet.settings.model.SettingsItem.ReferralBannerSettingsItem
import org.p2p.wallet.settings.model.SettingsItem.SettingsGroupTitleItem
import org.p2p.wallet.settings.model.SettingsItem.SettingsSpaceSeparatorItem
import org.p2p.wallet.settings.model.SettingsItem.SignOutButtonItem
import org.p2p.wallet.settings.model.SettingsItem.SwitchSettingsItem
import org.p2p.wallet.settings.model.SettingsItem.TextSettingsItem
import org.p2p.wallet.settings.ui.settings.adapter.viewholder.ComplexSettingsViewHolder
import org.p2p.wallet.settings.ui.settings.adapter.viewholder.NewSettingsViewHolder
import org.p2p.wallet.settings.ui.settings.adapter.viewholder.ReferralBannerSettingsViewHolder
import org.p2p.wallet.settings.ui.settings.adapter.viewholder.SettingsGroupTitleViewHolder
import org.p2p.wallet.settings.ui.settings.adapter.viewholder.SettingsSpaceSeparatorViewHolder
import org.p2p.wallet.settings.ui.settings.adapter.viewholder.SignOutButtonViewHolder
import org.p2p.wallet.settings.ui.settings.adapter.viewholder.SwitchSettingsViewHolder
import org.p2p.wallet.settings.ui.settings.adapter.viewholder.TextSettingsViewHolder
import org.p2p.wallet.utils.viewbinding.inflateViewBinding

class NewSettingsAdapter(
    private val onSettingsClicked: (SettingsItem) -> Unit,
    private val onReferralShareLinkClicked: () -> Unit,
    private val onReferralOpenDetailsClicked: () -> Unit,
) : RecyclerView.Adapter<NewSettingsViewHolder<*, *>>() {

    private val data = mutableListOf<SettingsItem>()

    fun setItems(newItems: List<SettingsItem>) {
        val old = data.toMutableList()
        data.clear()
        data.addAll(newItems)
        DiffUtil.calculateDiff(SettingsItemsDiffCallback(old, data)).dispatchUpdatesTo(this)
    }

    fun updateSwitchItem(itemId: Int, isSwitched: Boolean) {
        val itemIndex = data.indexOfFirst { it is SwitchSettingsItem && it.nameRes == itemId }
        if (itemIndex == -1) return

        val item = data[itemIndex] as SwitchSettingsItem
        val updatedItem = item.copy(isSwitched = isSwitched)
        data[itemIndex] = updatedItem
        notifyItemChanged(itemIndex, updatedItem)
    }

    override fun getItemViewType(position: Int): Int = when (data[position]) {
        is SettingsGroupTitleItem -> R.layout.item_settings_group_title
        is ComplexSettingsItem -> R.layout.item_settings_complex
        is SettingsSpaceSeparatorItem -> R.layout.item_settings_space_separator
        is SignOutButtonItem -> R.layout.item_settings_sign_out
        is SwitchSettingsItem -> R.layout.item_settings_switch
        is TextSettingsItem -> R.layout.item_settings_text_value
        is ReferralBannerSettingsItem -> R.layout.item_referral_banner
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewSettingsViewHolder<*, *> = when (viewType) {
        R.layout.item_settings_group_title -> {
            SettingsGroupTitleViewHolder(parent.inflateViewBindingForViewHolder())
        }
        R.layout.item_settings_complex -> {
            ComplexSettingsViewHolder(parent.inflateViewBindingForViewHolder(), onSettingsClicked)
        }
        R.layout.item_settings_space_separator -> {
            SettingsSpaceSeparatorViewHolder(parent.inflateViewBindingForViewHolder())
        }
        R.layout.item_settings_sign_out -> {
            SignOutButtonViewHolder(parent.inflateViewBindingForViewHolder(), onSettingsClicked)
        }
        R.layout.item_settings_switch -> {
            SwitchSettingsViewHolder(parent.inflateViewBindingForViewHolder(), onSettingsClicked)
        }
        R.layout.item_settings_text_value -> {
            TextSettingsViewHolder(parent.inflateViewBindingForViewHolder())
        }
        R.layout.item_referral_banner -> {
            ReferralBannerSettingsViewHolder(
                parent.inflateViewBindingForViewHolder(),
                onClickShareLink = onReferralShareLinkClicked,
                onClickOpenDetails = onReferralOpenDetailsClicked
            )
        }
        else -> {
            error("ViewHolder with view type $viewType is not found")
        }
    }

    override fun onBindViewHolder(holder: NewSettingsViewHolder<*, *>, position: Int) {
        holder.bind(data[position])
    }

    @Suppress("UNCHECKED_CAST")
    override fun onBindViewHolder(holder: NewSettingsViewHolder<*, *>, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }

        payloads.forEach { item ->
            when (item) {
                is SwitchSettingsItem -> (holder as SwitchSettingsViewHolder).bindSwitch(item)
            }
        }
    }

    override fun getItemCount(): Int = data.size

    private inline fun <reified VB : ViewBinding> ViewGroup.inflateViewBindingForViewHolder(): VB {
        return inflateViewBinding(attachToRoot = false)
    }
}
