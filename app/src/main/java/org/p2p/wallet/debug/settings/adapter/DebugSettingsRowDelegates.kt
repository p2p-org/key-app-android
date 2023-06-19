package org.p2p.wallet.debug.settings.adapter

import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.viewbinding.ViewBinding
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding
import org.p2p.uikit.utils.context
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.setTextColorRes
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemDebugSettingsRowItemBinding
import org.p2p.wallet.databinding.ItemSettingsDebugPopupBinding
import org.p2p.wallet.databinding.ItemSettingsDebugSwitchBinding
import org.p2p.wallet.databinding.ItemSettingsInfoBinding
import org.p2p.wallet.databinding.ItemSettingsLogoutBinding
import org.p2p.wallet.databinding.ItemSettingsTitleBinding
import org.p2p.wallet.settings.model.SettingsRow
import org.p2p.wallet.utils.withTextOrGone

private typealias SettingsRowClickListener = (titleResId: Int) -> Unit

fun settingsRowSectionItemDelegate(
    onItemClicked: SettingsRowClickListener
) = adapterDelegateViewBinding<SettingsRow.Section, SettingsRow, ItemDebugSettingsRowItemBinding>(
    viewBinding = ::inflateViewBinding
) {
    bind {
        with(binding) {
            textViewTop.setText(item.titleResId)

            item.subtitleRes
                ?.let(textViewBottom::setText)
                ?: textViewBottom.withTextOrGone(item.subtitle)
            item.subtitleTextColorRes
                ?.let { textViewBottom.setTextColorRes(it) }

            imageView.setImageResource(item.iconRes)
            bottomDivider.isVisible = item.isDivider

            root.setOnClickListener { onItemClicked.invoke(item.titleResId) }
        }
    }
}

fun settingsRowLogoutItemDelegate(
    onItemClicked: SettingsRowClickListener
) = adapterDelegateViewBinding<SettingsRow.Logout, SettingsRow, ItemSettingsLogoutBinding>(
    viewBinding = ::inflateViewBinding
) {
    bind {
        binding.actionButton.setOnClickListener { onItemClicked.invoke(R.string.settings_logout) }
    }
}

private typealias SettingsRowSwitchListener = (titleResId: Int, isSelected: Boolean) -> Unit

fun settingsRowSwtichItemDelegate(
    onSwitchClicked: SettingsRowSwitchListener
) = adapterDelegateViewBinding<SettingsRow.Switcher, SettingsRow, ItemSettingsDebugSwitchBinding>(
    viewBinding = ::inflateViewBinding
) {
    bind {
        with(binding) {
            textViewSettingsName.setText(item.titleResId)
            textViewSettingsSubtitle.text = item.subtitle.takeIf { !it.isNullOrEmpty() }
            imageViewSettingsIcon.setImageResource(item.iconRes)
            switchChangeSettings.isChecked = item.isSelected
            switchChangeSettings.setOnCheckedChangeListener { _, isChecked ->
                onSwitchClicked.invoke(item.titleResId, isChecked)
            }
            viewSeparator.isVisible = item.isDivider
        }
    }
}

fun settingsRowTitleItemDelegate() =
    adapterDelegateViewBinding<SettingsRow.Title, SettingsRow, ItemSettingsTitleBinding>(
        viewBinding = ::inflateViewBinding
    ) {
        bind {
            binding.textViewTop.setText(item.titleResId)
            binding.topDivider.isVisible = item.isDivider
        }
    }

fun settingsRowInfoItemDelegate(
    onItemClicked: SettingsRowClickListener
) = adapterDelegateViewBinding<SettingsRow.Info, SettingsRow, ItemSettingsInfoBinding>(
    viewBinding = ::inflateViewBinding
) {
    bind {
        binding.titleTextView.setText(item.titleResId)
        binding.valueTextView.text = item.subtitle
    }
}

fun settingsRowPopupMenuItemDelegate(
    onItemChanged: (String) -> Unit
) = adapterDelegateViewBinding<SettingsRow.PopupMenu, SettingsRow, ItemSettingsDebugPopupBinding>(
    viewBinding = ::inflateViewBinding
) {
    bind {
        val adapter = ArrayAdapter(
            binding.context,
            android.R.layout.simple_list_item_1,
            item.menuOptions.toTypedArray()
        )
        binding.inputLayoutMenu.setHint(item.titleResId)
        binding.textViewAutoComplete.setAdapter(adapter)
        binding.textViewAutoComplete.setText(item.selectedItem, false)
        binding.textViewAutoComplete.doOnTextChanged { text, _, _, _ ->
            onItemChanged.invoke(text?.toString().orEmpty())
        }
    }
}

private inline fun <reified V : ViewBinding> inflateViewBinding(inflater: LayoutInflater, parent: ViewGroup): V {
    return parent.inflateViewBinding(attachToRoot = false)
}
