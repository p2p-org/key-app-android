package org.p2p.uikit.atoms

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetStartAmountViewBinding
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.withImageOrGone
import org.p2p.uikit.utils.withTextOrGone

class UiKitStartAmountView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    @DrawableRes
    var icon: Int = 0
        set(value) {
            binding.imageView.withImageOrGone(value)
            field = value
        }

    var title: String? = null
        set(value) {
            binding.titleTextView.withTextOrGone(value)
            field = value
        }

    var subtitle1: String? = null
        set(value) {
            binding.subtitle1TextView.withTextOrGone(value)
            field = value
        }

    var subtitle2: String? = null
        set(value) {
            binding.subtitle2TextView.withTextOrGone(value)
            field = value
        }

    private val binding = inflateViewBinding<WidgetStartAmountViewBinding>()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitStartAmountView).use { typedArray ->
            val defaultSubtitleColor = context.getColor(R.color.text_mountain)
            val subtitleColorIndex = R.styleable.UiKitStartAmountView_subtitleColor
            val subtitleTextColor = typedArray.getColor(subtitleColorIndex, defaultSubtitleColor)

            icon = typedArray.getResourceId(R.styleable.UiKitStartAmountView_icon, 0)
            binding.subtitle1TextView.setTextColor(subtitleTextColor)
            title = typedArray.getString(R.styleable.UiKitStartAmountView_title)
            subtitle1 = typedArray.getString(R.styleable.UiKitStartAmountView_subtitle1)
            subtitle2 = typedArray.getString(R.styleable.UiKitStartAmountView_subtitle2)
        }
    }
}
