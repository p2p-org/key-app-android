package org.p2p.uikit.components

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import androidx.core.view.isVisible
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.Editable
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import org.p2p.core.common.TextContainer
import org.p2p.core.common.bind
import org.p2p.uikit.R
import org.p2p.uikit.UiKitEditTextStyleAttrsApplier
import org.p2p.uikit.databinding.WidgetUiKitEdittextBinding
import org.p2p.uikit.utils.SimpleTagTextWatcher
import org.p2p.uikit.utils.drawable.buildGradientDrawableRectangle
import org.p2p.uikit.utils.focusAndShowKeyboard
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.setTextColorRes

/**
 * Not resizable at the moment, but should be
 */
class UiKitEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private companion object {
        private const val CORNER_RADIUS = 20f
        private const val STROKE_WIDTH = 1
    }

    val input: EditText
        get() = binding.editTextField

    val text: Editable?
        get() = binding.editTextField.text

    val stringText: String?
        get() = text?.toString()

    var hint: CharSequence
        set(value) {
            binding.editTextField.hint = value
        }
        get() = binding.editTextField.hint

    val inputTextLength: Int
        get() = binding.editTextField.length()

    var clickListener: (() -> Unit)? = null
        set(value) {
            field = value
            if (value == null) {
                binding.editTextField.setOnClickListener(null)
            } else {
                binding.editTextField.setOnClickListener { value() }
            }
        }

    private val binding: WidgetUiKitEdittextBinding by lazy(LazyThreadSafetyMode.NONE, ::inflateViewBinding)

    private var editTextWatcher: SimpleTagTextWatcher? = null

    var currentViewTag: Any? = null
        set(value) {
            binding.root.tag = value
            field = value
        }

    private val styleAttrsApplier = UiKitEditTextStyleAttrsApplier()

    private val bgRed = buildGradientDrawableRectangle(
        cornerRadius = CORNER_RADIUS,
        strokeWidth = STROKE_WIDTH,
        strokeColor = context.getColor(R.color.bg_rose),
        backgroundColor = context.getColor(R.color.bg_snow)
    )

    private val bgNormal = buildGradientDrawableRectangle(
        cornerRadius = CORNER_RADIUS,
        strokeWidth = STROKE_WIDTH,
        strokeColor = context.getColor(R.color.bg_rain),
        backgroundColor = context.getColor(R.color.snow)
    )

    private val bgDisabled = buildGradientDrawableRectangle(
        cornerRadius = CORNER_RADIUS,
        strokeWidth = STROKE_WIDTH,
        strokeColor = context.getColor(R.color.bg_rain),
        backgroundColor = context.getColor(R.color.snow)
    )

    init {
        context.obtainStyledAttributes(attrs, R.styleable.UiKitEditText, 0, 0)
            .use { styleAttrsApplier.apply(it, binding) }

        binding.editTextField.isSaveEnabled = false
        binding.containerInputView.background = bgRed
    }

    override fun setEnabled(isEnable: Boolean) = with(binding) {
        super.setEnabled(isEnable)
        containerInputView.background = if (isEnable) bgNormal else bgDisabled
        editTextField.isEnabled = isEnabled
        editTextField.isFocusable = isEnable
        editTextField.isFocusableInTouchMode = isEnable
        editTextField.setTextColorRes(if (isEnable) R.color.text_night else R.color.text_night_30)
    }

    fun setText(text: String) = with(binding) {
        root.tag = null
        binding.editTextField.setText(text)
        root.tag = currentViewTag
    }

    fun setDigits(digits: String) {
        binding.editTextField.keyListener = DigitsKeyListener.getInstance(digits)
    }

    fun addOnTextChangedListener(block: (Editable) -> Unit) {
        val tag = currentViewTag ?: return
        editTextWatcher = object : SimpleTagTextWatcher(tag) {
            override fun afterTextChanged(tag: Any, text: Editable) {
                if (currentViewTag == tag) {
                    block(text)
                }
            }
        }
        binding.editTextField.addTextChangedListener(editTextWatcher)
    }

    fun focusAndShowKeyboard() {
        binding.editTextField.focusAndShowKeyboard()
    }

    /**
     * @param errorMessage pass null if you need to hide error
     */
    fun bindError(errorMessage: TextContainer?) = with(binding) {
        textViewError.isVisible = errorMessage != null
        errorMessage?.let(textViewError::bind)
        containerInputView.background = if (errorMessage != null) bgRed else bgNormal
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return UiKitEditTextSavedState(superState, text?.toString())
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is UiKitEditTextSavedState) {
            super.onRestoreInstanceState(state.superState)
            setText(state.text.orEmpty())
        } else {
            super.onRestoreInstanceState(state)
        }
    }
}

// Custom state class to hold the state of the custom EditText
private class UiKitEditTextSavedState : View.BaseSavedState {
    var text: String? = null

    constructor(superState: Parcelable?, text: String?) : super(superState) {
        this.text = text
    }

    constructor(source: Parcel) : super(source) {
        text = source.readString()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeString(text)
    }

    companion object CREATOR : Parcelable.Creator<UiKitEditTextSavedState> {
        override fun createFromParcel(source: Parcel): UiKitEditTextSavedState {
            return UiKitEditTextSavedState(source)
        }

        override fun newArray(size: Int): Array<UiKitEditTextSavedState?> {
            return arrayOfNulls(size)
        }
    }
}
