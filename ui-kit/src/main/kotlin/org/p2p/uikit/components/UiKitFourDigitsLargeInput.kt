package org.p2p.uikit.components

import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import androidx.core.view.isVisible
import android.content.Context
import android.util.AttributeSet
import com.redmadrobot.inputmask.MaskedTextChangedListener
import org.p2p.uikit.R
import org.p2p.uikit.databinding.ComponentLargeInputFourDigitsBinding
import org.p2p.uikit.utils.emptyString
import org.p2p.uikit.utils.inflateViewBinding

private val NO_ICON: Int? = null
private const val INPUT_MASK = "[000] [000]"

class UiKitFourDigitsLargeInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    interface Listener {
        fun onLeftIconClicked() = Unit
        fun onRightIconClicked() = Unit
        fun onInputChanged(inputValue: String) = Unit
    }

    @DrawableRes
    var leftIconRes: Int? = NO_ICON
        set(value) {
            value?.let { binding.leftIconImage.setImageResource(it) }
            binding.leftIconImage.isVisible = value != null
            field = value
        }

    var isChevronIconVisible: Boolean
        get() = binding.chevronImage.isVisible
        set(value) {
            binding.chevronImage.isVisible = value
        }

    var inputText: String
        get() = binding.fourDigitsInput.text?.toString().orEmpty()
        set(value) {
            binding.fourDigitsInput.setText(value)
        }

    @DrawableRes
    var rightIconRes: Int? = NO_ICON
        set(value) {
            value?.let { binding.rightIconImage.setImageResource(it) }
            binding.leftIconImage.isVisible = value != null
            field = value
        }

    private val binding: ComponentLargeInputFourDigitsBinding = inflateViewBinding()

    private var listener: Listener? = null

    private val onTextChangedListener = object : MaskedTextChangedListener.ValueListener {
        override fun onTextChanged(maskFilled: Boolean, extractedValue: String, formattedValue: String) {
            listener?.onInputChanged(formattedValue)
        }
    }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitFourDigitsLargeInput).use { typedArray ->
            rightIconRes = typedArray.getResourceId(R.styleable.UiKitFourDigitsLargeInput_rightIcon, -1)
                .takeIf { it != -1 }
            rightIconRes = typedArray.getResourceId(R.styleable.UiKitFourDigitsLargeInput_leftIcon, -1)
                .takeIf { it != -1 }
            isChevronIconVisible = typedArray.getBoolean(R.styleable.UiKitFourDigitsLargeInput_isChevronVisible, false)
        }

        with(binding.fourDigitsInput) {
            MaskedTextChangedListener.installOn(
                editText = this, primaryFormat = INPUT_MASK, valueListener = onTextChangedListener
            )
                .also { hint = it.placeholder() }
        }

        binding.leftIconImage.setOnClickListener { listener?.onLeftIconClicked() }
        binding.rightIconImage.setOnClickListener { listener?.onRightIconClicked() }
    }

    fun setInputListener(listener: Listener) {
        this.listener = listener
    }

    fun setErrorState(errorMessage: String) {
        binding.contentCardLayout.setBackgroundResource(R.drawable.background_input_stroke_oval_error)
        binding.errorText.isVisible = true
        binding.errorText.text = errorMessage
    }

    fun clearErrorState() {
        binding.contentCardLayout.setBackgroundResource(R.drawable.background_input_stroke_oval)
        binding.errorText.isVisible = false
        binding.errorText.text = emptyString()
    }
}
