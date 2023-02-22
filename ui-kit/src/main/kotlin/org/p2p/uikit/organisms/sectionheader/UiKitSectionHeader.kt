package org.p2p.uikit.organisms.sectionheader

import androidx.core.content.res.use
import androidx.core.view.isVisible
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import org.p2p.core.common.bind
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetSectionHeaderBinding
import org.p2p.uikit.utils.inflateViewBinding

internal class UiKitSectionHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var title: String? = null
        set(value) {
            binding.titleTextView.text = value
            field = value
        }

    var isIconVisible: Boolean = true
        set(value) {
            binding.chevronImageView.isVisible = value
            field = value
        }

    var isHidden: Boolean = false
        set(value) {
            rotateChevron(value)
            field = value
        }

    private val binding = inflateViewBinding<WidgetSectionHeaderBinding>()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitSectionHeader).use { typedArray ->
            val defaultColor = context.getColor(R.color.text_mountain)
            val tint = typedArray.getColor(R.styleable.UiKitSectionHeader_textIconTint, defaultColor)
            val isCaps = typedArray.getBoolean(R.styleable.UiKitSectionHeader_isCaps, false)
            isIconVisible = typedArray.getBoolean(R.styleable.UiKitSectionHeader_withIcon, true)
            title = typedArray.getString(R.styleable.UiKitSectionHeader_sectionText)

            binding.apply {
                chevronImageView.isVisible = isIconVisible
                titleTextView.isAllCaps = isCaps
                titleTextView.text = title
                titleTextView.setTextColor(tint)
                chevronImageView.setColorFilter(tint)
            }
        }
    }

    private fun rotateChevron(isHidden: Boolean) {
        val rotationValue = if (isHidden) 0f else 180f
        binding.chevronImageView
            .animate()
            .rotation(rotationValue)
            .start()
    }

    internal fun bind(model: SectionHeaderCellModel) {
        binding.titleTextView.bind(model.sectionTitle)
        binding.chevronImageView.isVisible = model.isShevronVisible
    }
}
