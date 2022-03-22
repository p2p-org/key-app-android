package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetProgressButtonBinding

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

        isClickable = true
        isFocusable = true

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProgressButton)

        val backgroundResource = typedArray.getResourceId(
            R.styleable.ProgressButton_buttonBackground, R.drawable.bg_blue_selector
        )

        setBackgroundResource(backgroundResource)

        val text = typedArray.getText(R.styleable.ProgressButton_buttonText)
        binding.actionTextView.text = text

        val startImageResourceId = typedArray.getResourceId(R.styleable.ProgressButton_buttonDrawable, 0)
        if (startImageResourceId != 0) {
            binding.startImageView.setImageResource(startImageResourceId)
            binding.startImageView.isVisible = true
        }

        val endImageResourceId = typedArray.getResourceId(R.styleable.ProgressButton_buttonDrawableEnd, 0)
        if (endImageResourceId != 0) {
            binding.endImageView.setImageResource(endImageResourceId)
            binding.endImageView.isVisible = true
        }

        val textAppearanceId = typedArray.getResourceId(
            R.styleable.ProgressButton_android_textAppearance,
            R.style.WalletTheme_TextAppearance_SemiBold16
        )
        if (textAppearanceId != 0) {
            binding.actionTextView.setTextAppearance(textAppearanceId)
        }

        val color = typedArray.getColor(
            R.styleable.ProgressButton_buttonTextColor, context.getColor(R.color.textIconButtonPrimary)
        )
        binding.actionTextView.setTextColor(color)

        val buttonDrawableTintId = typedArray.getResourceId(
            R.styleable.ProgressButton_buttonDrawableTint,
            R.color.textIconButtonPrimary
        )
        if (buttonDrawableTintId != 0) {
            val tintColor = context.getColor(buttonDrawableTintId)
            binding.startImageView.imageTintList = ColorStateList.valueOf(tintColor)
            binding.endImageView.imageTintList = ColorStateList.valueOf(tintColor)
        }
        val isEnabled = typedArray.getBoolean(R.styleable.ProgressButton_buttonEnabled, true)
        setEnabled(isEnabled)
        typedArray.recycle()
    }

    fun setLoading(isLoading: Boolean) {
        with(binding) {
            contentView.isInvisible = isLoading
            actionProgressBar.isVisible = isLoading
            isEnabled = !isLoading
        }
    }

    fun setStartIcon(@DrawableRes iconRes: Int?) {
        if (iconRes != null) {
            binding.startImageView.setImageResource(iconRes)
            binding.startImageView.isVisible = true
        } else {
            binding.startImageView.isVisible = false
        }
    }

    fun setDrawableEnd(@DrawableRes resId: Int?) {
        binding.endImageView.setImageResource(resId ?: 0)
        binding.endImageView.isVisible = resId != null
    }

    fun setActionText(@StringRes textRes: Int) {
        binding.actionTextView.setText(textRes)
    }

    fun setActionText(text: String) {
        binding.actionTextView.text = text
    }
}
