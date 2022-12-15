package org.p2p.uikit.components

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import org.p2p.core.textwatcher.AmountFractionTextWatcher
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetUikitEditTextBinding
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.uikit.utils.inflateViewBinding

private const val CORNER_RADIUS = 20f
private const val STROKE_WIDTH = 1

open class UiKitEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    protected val binding = inflateViewBinding<WidgetUikitEditTextBinding>()
    private var amountTextWatcher: AmountFractionTextWatcher? = null
    private var tokenTextWatcher: AmountFractionTextWatcher? = null

    private val bgRed = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = CORNER_RADIUS
        setColor(context.getColor(R.color.bg_rain))
        setStroke(STROKE_WIDTH, context.getColor(R.color.bg_rose))
    }

    private val bgNormal = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = CORNER_RADIUS
        setColor(context.getColor(R.color.rain))
        setStroke(STROKE_WIDTH, context.getColor(R.color.bg_rain))
    }

    init {
        val styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.UiKitEditText, 0, 0)
        val labelText = styleAttrs.getString(R.styleable.UiKitEditText_labelText).orEmpty()
        if (labelText.isNotEmpty()) {
            binding.textViewLabel.text = labelText
            binding.textViewLabel.isVisible = true
        }
        val hintText = styleAttrs.getString(R.styleable.UiKitEditText_hintText).orEmpty()
        if (hintText.isNotEmpty()) {
            binding.textViewHint.text = hintText
            binding.textViewHint.isVisible = true
        }
        val textAppearance = styleAttrs.getResourceId(R.styleable.UiKitEditText_android_textAppearance, -1)
        if (textAppearance != -1) {
            binding.editText.setTextAppearance(textAppearance)
            binding.textViewHint.setTextAppearance(textAppearance)
        }
        val text = styleAttrs.getText(R.styleable.UiKitEditText_android_text)
        if (!text.isNullOrEmpty()) {
            binding.editText.setText(text)
        }

        val hint = styleAttrs.getText(R.styleable.UiKitEditText_hintText)
        if (!hint.isNullOrEmpty()) {
            binding.textViewHint.text = hint
        }

        binding.inputViewContainer.background = bgNormal
        styleAttrs.recycle()
    }

    fun setText(text: String) {
        binding.editText.setText(text)
        val selectionIndex = if (text.isNotEmpty()) text.length else 0
        binding.editText.setSelection(selectionIndex)
    }

    fun setHint(hint: String) {
        binding.textViewHint.text = hint
    }

    val text: Editable?
        get() = binding.editText.text

    val stringText: String
        get() = text?.toString().orEmpty()

    val hint: CharSequence
        get() = binding.editText.hint

    val length: Int
        get() = binding.editText.length()

    fun showError(isVisible: Boolean) = with(binding) {
        inputViewContainer.background = if (isVisible) bgRed else bgNormal
    }

    fun focusAndShowKeyboard() {
        binding.editText.focusAndShowKeyboard()
    }

    fun setOnTokenAmountChangeListener(onTokenAmountChange: (String) -> Unit) {
        tokenTextWatcher = AmountFractionTextWatcher.installOn(binding.editText) {
            val amountWithoutSpaces = it.replace(" ", "")
            onTokenAmountChange(amountWithoutSpaces)
        }
    }

    fun setOnCurrencyAmountChangeListener(onCurrencyAmountChange: (String) -> Unit) {
        amountTextWatcher = AmountFractionTextWatcher.installOn(binding.editText) {
            val amountWithoutSpaces = it.replace(" ", "")
            onCurrencyAmountChange(amountWithoutSpaces)
        }
    }

    fun setTokenAmount(tokenAmount: String?) = with(binding.editText) {
        removeTextChangedListener(tokenTextWatcher)
        setText(tokenAmount.orEmpty())
        addTextChangedListener(tokenTextWatcher)
    }

    fun setCurrencyAmount(currencyAmount: String?) = with(binding.editText) {
        removeTextChangedListener(amountTextWatcher)
        setText(currencyAmount.orEmpty())
        addTextChangedListener(amountTextWatcher)
    }
}
