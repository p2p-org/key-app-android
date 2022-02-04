package org.p2p.wallet.settings.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import org.p2p.wallet.databinding.WidgetSettingsRowBinding
import org.p2p.wallet.settings.model.SettingItem

class SettingsRowView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = WidgetSettingsRowBinding.inflate(LayoutInflater.from(context), this, true)

    fun setup(items: List<SettingItem>) {
        binding.container.removeAllViews()
        items.forEach {
            val settingRow = SettingsRowItemView(context).apply { setup(it) }
            binding.container.addView(settingRow)
        }
    }
}