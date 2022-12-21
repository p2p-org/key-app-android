package org.p2p.uikit.components

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.InputType
import android.util.AttributeSet
import org.p2p.core.textwatcher.AmountFractionTextWatcher
import org.p2p.uikit.R
import org.p2p.uikit.databinding.WidgetUiKitAmountEditTextBinding
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.uikit.utils.inflateViewBinding

private const val CORNER_RADIUS = 20f
private const val STROKE_WIDTH = 1

open class UiKitAmountEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    var isEditable: Boolean
        get() = binding.editText.inputType != InputType.TYPE_NULL
        set(value) {
            binding.editText.inputType = if (value) InputType.TYPE_CLASS_PHONE else InputType.TYPE_NULL
        }

    val text: Editable?
        get() = binding.editText.text

    val stringText: String
        get() = text?.toString().orEmpty()

    val hint: CharSequence
        get() = binding.editText.hint

    val length: Int
        get() = binding.editText.length()

    protected val binding = inflateViewBinding<WidgetUiKitAmountEditTextBinding>()

    private var inputTextWatcher: AmountFractionTextWatcher? = null

    private val bgRed = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = CORNER_RADIUS
        setColor(context.getColor(R.color.bg_snow))
        setStroke(STROKE_WIDTH, context.getColor(R.color.bg_rose))
    }

    private val bgNormal = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = CORNER_RADIUS
        setColor(context.getColor(R.color.bg_snow))
        setStroke(STROKE_WIDTH, context.getColor(R.color.bg_snow))
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

    fun setHint(hint: String) {
        binding.textViewHint.text = hint
    }

    fun showError(isVisible: Boolean) {
        binding.inputViewContainer.background = if (isVisible) bgRed else bgNormal
    }

    fun focusAndShowKeyboard() {
        binding.editText.focusAndShowKeyboard()
    }

    fun setAmountInputTextWatcher(onAmountChange: (String) -> Unit) {
        inputTextWatcher = AmountFractionTextWatcher.installOn(binding.editText, maxDecimalsAllowed = 2) {
            val amountWithoutSpaces = it.replace(" ", "")
            onAmountChange(amountWithoutSpaces)
        }
    }

    fun setAmount(formattedAmount: String) {
        setupText(formattedAmount)
    }

    fun setupText(text: String) {
        binding.editText.setText(text)
        val selectionIndex = if (text.isNotEmpty()) text.length else 0
        binding.editText.setSelection(selectionIndex)
    }
}
