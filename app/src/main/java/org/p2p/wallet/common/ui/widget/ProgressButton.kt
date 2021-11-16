package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetProgressButtonBinding
import org.p2p.wallet.utils.colorFromTheme

class ProgressButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: WidgetProgressButtonBinding = WidgetProgressButtonBinding.inflate(
        LayoutInflater.from(context), this
    )

    init {
        val outValue = TypedValue()
        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        foreground = getDrawable(context, outValue.resourceId)
        clipToOutline = true

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton)

        val backgroundResource = typedArray.getResourceId(
            R.styleable.ProgressButton_buttonBackground, R.drawable.bg_blue_selector
        )

        setBackgroundResource(backgroundResource)

        val text = typedArray.getText(R.styleable.ProgressButton_buttonText)
        binding.actionTextView.text = text

        val color = typedArray.getColor(
            R.styleable.ProgressButton_buttonTextColor, colorFromTheme(R.attr.colorElementPrimary)
        )
        binding.actionTextView.setTextColor(color)

        val imageResourceId = typedArray.getResourceId(R.styleable.ProgressButton_buttonDrawable, 0)
        if (imageResourceId != 0) {
            binding.actionImageView.setImageResource(imageResourceId)
        }
        typedArray.recycle()
    }

    fun setLoading(isLoading: Boolean) {
        with(binding) {
            contentView.isInvisible = isLoading
            actionProgressBar.isVisible = isLoading
            isEnabled = !isLoading
        }
    }

    fun setActionText(@StringRes textRes: Int) {
        binding.actionTextView.setText(textRes)
    }
}