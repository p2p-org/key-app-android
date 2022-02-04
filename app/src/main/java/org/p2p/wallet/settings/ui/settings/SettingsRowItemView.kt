package org.p2p.wallet.settings.ui.settings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import org.p2p.wallet.databinding.ItemSettingsRowItemBinding
import org.p2p.wallet.settings.model.SettingItem

class SettingsRowItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val binding = ItemSettingsRowItemBinding.inflate(LayoutInflater.from(context), this)

    fun setup(item: SettingItem) {
        with(binding) {
            binding.imageView.setImageResource(item.iconRes)
            binding.topTextView.setText(item.titleRes)
            binding.bottomTextView.setText(item.subtitleRes)
            setOnClickListener { item.onItemClickListener?.invoke(item) }
        }
    }
}