package org.p2p.uikit.organisms

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetSectionHeaderBinding

class SectionHeader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = WidgetSectionHeaderBinding.inflate(LayoutInflater.from(context), this)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.SectionHeader).use { typedArray ->
            val withIcon = typedArray.getBoolean(R.styleable.SectionHeader_withIcon, true)
            val defaultColor = context.getColor(R.color.text_mountain)
            val tint = typedArray.getColor(R.styleable.SectionHeader_textIconTint, defaultColor)
            val text = typedArray.getString(R.styleable.SectionHeader_sectionText)
            val isCaps = typedArray.getBoolean(R.styleable.SectionHeader_isCaps, false)

            binding.apply {
                chevronImageView.isVisible = withIcon
                titleTextView.isAllCaps = isCaps
                titleTextView.text = text
                titleTextView.setTextColor(tint)
                chevronImageView.setColorFilter(tint)
            }
        }
    }

    fun setText(text: String) {
        binding.titleTextView.text = text
    }

    fun showIcon(isVisible: Boolean) {
        binding.chevronImageView.isVisible = isVisible
    }

    fun setHidden(isHidden: Boolean) {
        val rotationValue = if (isHidden) 0f else 180f
        binding.chevronImageView
            .animate()
            .rotation(rotationValue)
            .start()
    }
}
