package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetDoubleTextViewBinding
import org.p2p.wallet.utils.colorFromTheme

class DoubleTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = WidgetDoubleTextViewBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        orientation = HORIZONTAL

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DoubleTextView)

        val topText = typedArray.getText(R.styleable.DoubleTextView_topText)
        binding.topTextView.text = topText

        val bottomText = typedArray.getText(R.styleable.DoubleTextView_bottomText)
        binding.bottomTextView.text = bottomText

        val endImageResourceId = typedArray.getResourceId(R.styleable.DoubleTextView_drawableEnd, 0)
        if (endImageResourceId != 0) {
            binding.iconImageView.isVisible = true
            binding.iconImageView.setImageResource(endImageResourceId)
        }
        typedArray.recycle()
    }

    fun setTopText(text: String) {
        binding.topTextView.text = text
    }

    fun setBottomText(text: String) {
        binding.bottomTextView.text = text
    }

    fun setBottomText(@StringRes text: Int) {
        binding.bottomTextView.setText(text)
    }

    fun setBottomTextColor(@ColorRes colorRes: Int) {
        binding.bottomTextView.setTextColor(context.getColor(colorRes))
    }

    fun setBottomTextColorFromTheme(@AttrRes colorAttrId: Int) {
        binding.bottomTextView.setTextColor(colorFromTheme(colorAttrId))
    }

    fun setDrawableEnd(@DrawableRes icon: Int?) {
        if (icon != null && icon != 0) {
            binding.iconImageView.isVisible = true
            binding.iconImageView.setImageResource(icon)
        } else {
            binding.iconImageView.isVisible = false
        }
    }
}
