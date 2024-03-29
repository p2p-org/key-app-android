package org.p2p.wallet.common.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.widget.TextViewCompat
import org.p2p.uikit.utils.getColor
import org.p2p.wallet.R
import org.p2p.wallet.databinding.WidgetOptionsTextViewBinding
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import org.p2p.wallet.utils.withTextOrGone

class OptionsTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding: WidgetOptionsTextViewBinding = inflateViewBinding()

    var labelText: CharSequence
        get() = binding.labelTextView.text
        set(value) {
            binding.labelTextView.text = value
        }

    init {
        orientation = HORIZONTAL

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.OptionsTextView)

        val labelText = typedArray.getText(R.styleable.OptionsTextView_optionsLabelText)
        binding.labelTextView.text = labelText

        val optionsText = typedArray.getText(R.styleable.OptionsTextView_optionsText)
        binding.optionsTextView withTextOrGone optionsText

        val optionsValueText = typedArray.getText(R.styleable.OptionsTextView_optionsValueText)
        binding.valueTextView.text = optionsValueText

        val optionsValueTextColor = typedArray.getColor(
            R.styleable.OptionsTextView_optionsValueTextColor,
            getColor(R.color.textIconPrimary)
        )
        binding.valueTextView.setTextColor(optionsValueTextColor)

        val isBoldLabel = typedArray.getBoolean(R.styleable.OptionsTextView_isBoldLabel, false)
        if (isBoldLabel) {
            TextViewCompat.setTextAppearance(
                binding.labelTextView,
                R.style.UiKit_TextAppearance_SemiBold_Text3
            )
        }

        val isBoldValue = typedArray.getBoolean(R.styleable.OptionsTextView_isBoldValue, false)
        if (isBoldValue) {
            TextViewCompat.setTextAppearance(
                binding.valueTextView,
                R.style.UiKit_TextAppearance_SemiBold_Text3
            )
        }

        typedArray.recycle()
    }

    fun setLabelText(@StringRes label: Int) {
        binding.labelTextView.setText(label)
    }

    fun setLabelTextColor(@ColorInt color: Int) {
        binding.labelTextView.setTextColor(color)
    }

    fun setOptionsText(@StringRes label: Int) {
        val labelText = context.getString(label)
        binding.optionsTextView.withTextOrGone(labelText)
    }

    fun setValueText(text: String) {
        binding.valueTextView.text = text
    }

    fun setValueTextColor(@ColorInt color: Int) {
        binding.valueTextView.setTextColor(color)
    }
}
