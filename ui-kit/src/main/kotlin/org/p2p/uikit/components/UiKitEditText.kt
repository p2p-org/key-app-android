package org.p2p.uikit.components

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.getDimensionPixelSizeOrThrow
import androidx.core.content.res.getIntOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.view.isVisible
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.Parcelable
import android.text.Editable
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import org.p2p.core.common.TextContainer
import org.p2p.core.common.bind
import org.p2p.uikit.R
import org.p2p.uikit.components.edittext.UiKitEditTextSavedState
import org.p2p.uikit.databinding.WidgetUiKitEdittextBinding
import org.p2p.uikit.utils.SimpleTagTextWatcher
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.uikit.utils.inflateViewBinding

@Deprecated(
    message = "Not resizable and extendable",
    replaceWith = ReplaceWith(
        "NewUiKitEditText",
        "org.p2p.uikit.components.edittext.v2.NewUiKitEditText"
    )
)
class UiKitEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private companion object {
        private const val CORNER_RADIUS = 20f
        private const val STROKE_WIDTH = 1
    }

    private val binding by lazy(LazyThreadSafetyMode.NONE) { inflateViewBinding<WidgetUiKitEdittextBinding>() }

    private var editTextWatcher: SimpleTagTextWatcher? = null

    private var viewTag: Any? = null

    private val bgError = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = CORNER_RADIUS
        setColor(context.getColor(R.color.bg_snow))
        setStroke(STROKE_WIDTH, context.getColor(R.color.bg_rose))
    }

    private val bgNormal = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = CORNER_RADIUS
        setColor(context.getColor(R.color.snow))
        setStroke(STROKE_WIDTH, context.getColor(R.color.bg_rain))
    }

    private val bgDisabled = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = CORNER_RADIUS
        setColor(context.getColor(R.color.snow))
        setStroke(STROKE_WIDTH, context.getColor(R.color.bg_rain))
    }

    val input: HintEditText
        get() = binding.editTextField

    val text: Editable?
        get() = binding.editTextField.text

    val stringText: String?
        get() = text?.toString()

    val hint: CharSequence
        get() = binding.editTextField.hint

    val length: Int
        get() = binding.editTextField.length()

    init {
        val styleAttrs = context.obtainStyledAttributes(attrs, R.styleable.UiKitEditText, 0, 0)
        val labelText = styleAttrs.getString(R.styleable.UiKitEditText_labelText).orEmpty()
        binding.textViewLabel.text = labelText
        binding.textViewLabel.isVisible = labelText.isNotEmpty()

        val hintText = styleAttrs.getString(R.styleable.UiKitEditText_hintText).orEmpty()
        binding.editTextField.hint = hintText

        val tipText = styleAttrs.getString(R.styleable.UiKitEditText_tipText).orEmpty()
        binding.textViewTip.text = tipText
        binding.textViewTip.isVisible = tipText.isNotEmpty()

        val tipTextColor = styleAttrs.getColor(R.styleable.UiKitEditText_tipTextColor, -1)
        if (tipTextColor != -1) {
            binding.textViewTip.setTextColor(tipTextColor)
        }

        val textAppearance = styleAttrs.getResourceId(R.styleable.UiKitEditText_android_textAppearance, -1)
        if (textAppearance != -1) {
            binding.editTextField.setTextAppearance(textAppearance)
        }

        val text = styleAttrs.getText(R.styleable.UiKitEditText_android_text)
        if (!text.isNullOrEmpty()) {
            binding.editTextField.setText(text)
        }

        isEnabled = styleAttrs.getBoolean(R.styleable.UiKitEditText_android_enabled, true)

        binding.containerInputView.background = bgNormal
        val isDropdown = styleAttrs.getBoolean(R.styleable.UiKitEditText_isDropdown, false)
        if (isDropdown) {
            binding.editTextField.isFocusable = false
            binding.imageViewArrow.isVisible = true
            binding.editTextField.isClickable = true
        }

        if (styleAttrs.hasValue(R.styleable.UiKitEditText_android_inputType)) {
            val inputType = styleAttrs.getIntOrThrow(R.styleable.UiKitEditText_android_inputType)
            binding.editTextField.inputType = inputType
        }
        if (styleAttrs.hasValue(R.styleable.UiKitEditText_android_digits)) {
            val digits = styleAttrs.getString(R.styleable.UiKitEditText_android_digits)
            digits?.let(::setDigits)
        }
        if (styleAttrs.hasValue(R.styleable.UiKitEditText_android_nextFocusDown)) {
            val nextFocusDown = styleAttrs.getResourceIdOrThrow(R.styleable.UiKitEditText_android_nextFocusDown)
            binding.editTextField.nextFocusDownId = nextFocusDown
        }
        if (styleAttrs.hasValue(R.styleable.UiKitEditText_android_imeOptions)) {
            val imeOptions = styleAttrs.getIntOrThrow(R.styleable.UiKitEditText_android_imeOptions)
            binding.editTextField.imeOptions = imeOptions
        }
        if (styleAttrs.hasValue(R.styleable.UiKitEditText_cornerRadius)) {
            val cornerRadius = styleAttrs
                .getDimensionPixelSizeOrThrow(R.styleable.UiKitEditText_cornerRadius)
                .toFloat()

            setOf(bgNormal, bgDisabled, bgError)
                .forEach { it.cornerRadius = cornerRadius }
        }

        styleAttrs.recycle()

        binding.editTextField.isSaveEnabled = false
    }

    override fun setEnabled(isEnable: Boolean) {
        super.setEnabled(isEnable)
        binding.containerInputView.background = if (isEnable) bgNormal else bgDisabled
        binding.editTextField.isEnabled = isEnabled
        binding.editTextField.isFocusable = isEnable
        binding.editTextField.isFocusableInTouchMode = isEnable
        val textColor = context.getColor(if (isEnable) R.color.text_night else R.color.text_night_30)
        binding.editTextField.setTextColor(textColor)
    }

    fun setText(text: String) {
        binding.root.tag = null
        binding.editTextField.setText(text)
        binding.root.tag = viewTag
    }

    fun setViewTag(tag: Any?) {
        binding.root.tag = tag
        viewTag = tag
    }

    fun setHint(hint: String) {
        binding.editTextField.hint = hint
    }

    fun setDigits(digits: String) {
        binding.editTextField.keyListener = DigitsKeyListener.getInstance(digits)
    }

    fun addOnTextChangedListener(block: (Editable) -> Unit) {
        val tag = viewTag ?: return
        editTextWatcher = object : SimpleTagTextWatcher(tag) {
            override fun afterTextChanged(tag: Any, text: Editable) {
                if (viewTag == tag) {
                    block(text)
                }
            }
        }
        binding.editTextField.addTextChangedListener(editTextWatcher)
    }

    fun focusAndShowKeyboard() {
        binding.editTextField.focusAndShowKeyboard()
    }

    fun bindError(errorMessage: TextContainer?) {
        with(binding) {
            textViewError.isVisible = errorMessage != null
            textViewTip.isVisible = errorMessage == null
            errorMessage?.let { textViewError.bind(it) }
            containerInputView.background = if (errorMessage != null) bgError else bgNormal
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return UiKitEditTextSavedState(superState, text?.toString())
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is UiKitEditTextSavedState) {
            super.onRestoreInstanceState(state.superState)
            setText(state.text ?: "")
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    fun setOnClickListener(block: () -> Unit) {
        binding.editTextField.setOnClickListener { block() }
    }
}
