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
import org.p2p.uikit.utils.colorFromTheme

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
        binding.textViewTop.text = topText

        val bottomText = typedArray.getText(R.styleable.DoubleTextView_bottomText)
        binding.textViewTop.text = bottomText

        val endImageResourceId = typedArray.getResourceId(R.styleable.DoubleTextView_drawableEnd, 0)
        if (endImageResourceId != 0) {
            binding.iconImageView.isVisible = true
            binding.iconImageView.setImageResource(endImageResourceId)
        }
        typedArray.recycle()
    }

    fun setTopText(text: String) {
        binding.textViewTop.text = text
    }

    fun setBottomText(text: String) {
        binding.textViewBottom.text = text
    }

    fun setBottomText(@StringRes text: Int) {
        binding.textViewBottom.setText(text)
    }

    fun setBottomTextColor(@ColorRes colorRes: Int) {
        binding.textViewBottom.setTextColor(context.getColor(colorRes))
    }

    fun setBottomTextColorFromTheme(@AttrRes colorAttrId: Int) {
        binding.textViewBottom.setTextColor(colorFromTheme(colorAttrId))
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
