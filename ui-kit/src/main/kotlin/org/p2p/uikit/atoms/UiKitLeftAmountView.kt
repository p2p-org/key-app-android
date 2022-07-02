package org.p2p.uikit.atoms

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetLeftAmountViewBinding
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.withImageOrGone
import org.p2p.uikit.utils.withTextOrGone

class UiKitLeftAmountView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    @DrawableRes
    var icon: Int? = null
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

    private val binding = inflateViewBinding<WidgetLeftAmountViewBinding>()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitLeftAmountView).use { typedArray ->
            val iconRes = typedArray.getResourceId(R.styleable.UiKitLeftAmountView_icon, -1)
            val defaultSubtitleColor = ContextCompat.getColor(context, R.color.text_mountain)
            val subtitleTextColor =
                typedArray.getColor(R.styleable.UiKitLeftAmountView_subtitleColor, defaultSubtitleColor)

            icon = if (iconRes == -1) null else iconRes
            binding.subtitle1TextView.setTextColor(subtitleTextColor)
            title = typedArray.getString(R.styleable.UiKitLeftAmountView_title)
            subtitle1 = typedArray.getString(R.styleable.UiKitLeftAmountView_subtitle1)
            subtitle2 = typedArray.getString(R.styleable.UiKitLeftAmountView_subtitle2)
        }
    }
}
