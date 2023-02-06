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

    var usdAmount: String? = null
        set(value) {
            binding.usdAmountTextView.withTextOrGone(value)
            field = value
        }

    var tokenAmount: String? = null
        set(value) {
            binding.tokenAmountTextView.withTextOrGone(value)
            field = value
        }

    fun setTokenAmountTextColor(@ColorInt color: Int) {
        binding.tokenAmountTextView.setTextColor(color)
    }

    @DrawableRes
    var icon: Int? = null
        set(value) {
            binding.actionImageView.withImageOrGone(value)
            field = value
        }

    private val binding = inflateViewBinding<WidgetEndAmountViewBinding>()

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitEndAmountView).use { typedArray ->
            icon = typedArray.getResourceId(R.styleable.UiKitEndAmountView_icon, 0)
            usdAmount = typedArray.getString(R.styleable.UiKitEndAmountView_usdAmount)
            tokenAmount = typedArray.getString(R.styleable.UiKitEndAmountView_tokenAmount)
        }
    }
}
