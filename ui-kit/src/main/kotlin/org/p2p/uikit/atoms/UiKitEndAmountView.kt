package org.p2p.uikit.atoms

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetEndAmountViewBinding
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.withImageOrGone
import org.p2p.uikit.utils.withTextOrGone

class UiKitEndAmountView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var topValue: String? = null
        set(value) {
            binding.textViewTopValue.withTextOrGone(value)
            field = value
        }

    fun setTopValueTextColor(@ColorInt color: Int) {
        binding.textViewTopValue.setTextColor(color)
    }

    var bottomValue: String? = null
        set(value) {
            binding.textViewBottomValue.withTextOrGone(value)
            field = value
        }

    fun setBottomValueTextColor(@ColorInt color: Int) {
        binding.textViewBottomValue.setTextColor(color)
    }

    @DrawableRes
    var icon: Int? = null
        set(value) {
            binding.imageViewAction.withImageOrGone(value)
            field = value
        }

    private val binding = inflateViewBinding<WidgetEndAmountViewBinding>()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitEndAmountView).use { typedArray ->
            icon = typedArray.getResourceId(R.styleable.UiKitEndAmountView_icon, 0)
            topValue = typedArray.getString(R.styleable.UiKitEndAmountView_topValue)
            bottomValue = typedArray.getString(R.styleable.UiKitEndAmountView_bottomValue)
        }
    }
}
